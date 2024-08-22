package pm.serviceImplements;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.dto.TaskActivityResponse;
import pm.model.product.BussinessCategory;
import pm.model.task.GroupActivityDesignation;
import pm.model.task.TaskCategory;
import pm.model.users.Roles;
import pm.model.users.Users;
import pm.repository.GroupActivityDesignationRepository;
import pm.repository.RolesRepository;
import pm.repository.TaskCategoryRepository;
import pm.repository.UsersRepository;
import pm.response.ApiResponse;
import pm.response.TaskCategoryResponse;
import pm.service.TaskCategoryService;
import pm.utils.AuthUserData;

@Service
public class TaskCategoryImpl implements TaskCategoryService {

    @Autowired
    private TaskCategoryRepository taskCategoryRepository;

    @Autowired
    private GroupActivityDesignationRepository groupActivityDesignationRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Override
    public ResponseEntity<?> create(String groupName, String category) {
        LocalDateTime currDateTime = LocalDateTime.now();
        // Validate the name field using the regular expression
        Stream<String> groupNamesStream = Arrays.stream(category.split(","));

        // Use distinct() to remove consecutive repetitions
        String modifiedName = groupNamesStream.distinct().collect(Collectors.joining(","));

        if (!category.equals(modifiedName)) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Activity name contains duplicate entries.", null));
        }
        // if (name == null) {
        // return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        // .body(new ApiResponse(false, "Name cannot be null", null));
        // }
        // Check for blank name
        if (groupName == null || groupName.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Group Name cannot be blank", null));
        }

        if (!groupName.matches("^(?!\\s)[A-Za-z\\s\\-\\&/]*[A-Za-z][A-Za-z\\s\\-\\&/]*$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Group Name must contain only alphabetic letters", null));
        }
        if (groupName.length() > 255) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Group Name exceeds maximum length of 255 characters", null));
        }
        String trimmedName = groupName.replaceAll("\\s+", " ");

        Optional<TaskCategory> taskCategory1 = taskCategoryRepository
                .findByTaskNameAndIs_deletedFalse(trimmedName);
        if (taskCategory1.isPresent()) {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
                    .body(new ApiResponse(false, "Group Name already exist ", Collections.emptyList()));
        }
        TaskCategory taskCategory = new TaskCategory();
        taskCategory.setGroupName(groupName);
        taskCategory.setCategory(category);
        taskCategory.setCreatedAt(currDateTime);
        taskCategory.setUpdatedAt(currDateTime);
        taskCategory.setDeleted(false);

        taskCategory.setStatus(true);
        taskCategory = taskCategoryRepository.save(taskCategory);

        String message = "Task Activity created successfully.";
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, taskCategory));
    }

    @Override
    public ResponseEntity<?> list(int page, int size, boolean search, String value) {
        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);

        Page<TaskCategory> taskCategories = null;
        if (search) {
            taskCategories = taskCategoryRepository.getallTaskCategoryActiveWithSearch(value, pageable);

        } else {

            taskCategories = taskCategoryRepository.getallTaskCategoryActiveWithPage(pageable);
        }

        String message = "Task Activity Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, taskCategories));
    }

    @Override
    public ResponseEntity<?> byId(int id) {
        TaskCategoryResponse categoryResponse = new TaskCategoryResponse();
        TaskCategory taskCategory = taskCategoryRepository.getByIdisActive(id).orElse(null);

        Optional<GroupActivityDesignation> activityDesignationOptional = groupActivityDesignationRepository
                .findByGroupId(taskCategory.getId());

        if (activityDesignationOptional.isPresent()) {
            GroupActivityDesignation activityDesignation = activityDesignationOptional.get();

            List<Roles> roles = rolesRepository.findAllRolesparticular(activityDesignation.getRoleId());
            Integer[] rolename = roles.stream().map(Roles::getId).toArray(Integer[]::new);
            String roleIds = roles.stream().map(Roles::getId).map(String::valueOf).collect(Collectors.joining(","));

            BeanUtils.copyProperties(taskCategory, categoryResponse);

            String[] categories = taskCategory.getCategory().split(",");

            // Trim each category to remove leading and trailing whitespaces
            for (int i = 0; i < categories.length; i++) {
                categories[i] = categories[i].trim();
            }
            categoryResponse.setCategories(categories);
            categoryResponse.setDesignations(rolename);
            categoryResponse.setRoleIds(roleIds);
            String message = "Task Activity Fetched Successfully.";
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, categoryResponse));
        } else {
            // Handle the case when the Optional is empty
            BeanUtils.copyProperties(taskCategory, categoryResponse);

            String[] categorieselse = taskCategory.getCategory().split(",");

            // Trim each category to remove leading and trailing whitespaces
            for (int i = 0; i < categorieselse.length; i++) {
                categorieselse[i] = categorieselse[i].trim();
            }
            categoryResponse.setCategories(categorieselse);
            String message = "No GroupActivityDesignation found for the given Task Category ID.";
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(false, message, categoryResponse));
        }
    }

    @Override
    public ResponseEntity<?> update(TaskCategory taskCategory, int id) {
        // Validate the name field using the regular expression
        String name = taskCategory.getGroupName();
        if (!name.matches("^(?!\\s)[A-Za-z\\s\\-\\&/]*[A-Za-z][A-Za-z\\s\\-\\&/]*$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Group Name must contain only alphabetic letters", null));
        }
        if (name.length() > 255) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Group Name exceeds maximum length of 255 characters", null));
        }
        // Check if the Task Category is Already exists
        Optional<TaskCategory> existingTaskCategory = taskCategoryRepository.findByTaskNameAndIs_deletedFalse(name);
        // If the categry with the same name is exist
        if (existingTaskCategory.isPresent() && existingTaskCategory.get().getId() != id) {
            String errorMessage = "Task Activity with Group Name '" + name + "' already exists.";
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, errorMessage, null));
        }

        Stream<String> groupNamesStream = Arrays.stream(taskCategory.getCategory().split(","));

        // Use distinct() to remove consecutive repetitions
        String modifiedName = groupNamesStream.distinct().collect(Collectors.joining(","));

        if (!taskCategory.getCategory().equals(modifiedName)) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Activity name contains duplicate entries.", null));
        }

        String newName = taskCategory.getGroupName();
        String trimmedName = name.replaceAll("\\s+", " ");
        if (!taskCategory.getGroupName().equals(newName)) {
            Optional<TaskCategory> taskName = taskCategoryRepository.findByTaskNameAndIs_deletedFalse(trimmedName);
            if (taskName.isPresent() && taskName.get().getId() != taskCategory.getId()) {
                String message = "Task Activity Name is already exists.";
                return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(new ApiResponse(false, message, null));
            }
        }

        LocalDateTime currDateTime = LocalDateTime.now();
        taskCategory.setCreatedAt(currDateTime);
        taskCategory.setUpdatedAt(currDateTime);
        taskCategory.setDeleted(false);

        taskCategory.setStatus(true);
        taskCategory = taskCategoryRepository.save(taskCategory);
        String message = "Task Activity Updated Successfully.";
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, null));
    }

    @Override
    public ResponseEntity<?> groupMappingDesignation(int groupId, String roleId) {
        Optional<GroupActivityDesignation> existingRecordOptional = groupActivityDesignationRepository
                .findByGroupId(groupId);

        GroupActivityDesignation groupActivityDesignation;

        if (existingRecordOptional.isPresent()) {
            // If the record exists, update it
            GroupActivityDesignation existingEntity = existingRecordOptional.get();
            existingEntity.setUpdatedAt(LocalDateTime.now());
            existingEntity.setRoleId(roleId);
            existingEntity.setStatus(true);
            groupActivityDesignation = groupActivityDesignationRepository.save(existingEntity);
        } else {
            // If the record doesn't exist, save a new one
            groupActivityDesignation = new GroupActivityDesignation();
            groupActivityDesignation.setCreatedAt(LocalDateTime.now());
            groupActivityDesignation.setUpdatedAt(LocalDateTime.now());
            groupActivityDesignation.setGroupId(groupId);
            groupActivityDesignation.setRoleId(roleId);
            groupActivityDesignation.setStatus(true);
            groupActivityDesignation = groupActivityDesignationRepository.save(groupActivityDesignation);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Created", groupActivityDesignation));

    }

    @Override
    public ResponseEntity<?> getCategoriesroles(int id) {
        List<TaskCategory> taskCategories = taskCategoryRepository.getallTaskCategoryActive();
        taskCategories.sort(Comparator.comparingInt(TaskCategory::getId).reversed());

        StringBuilder roleIdsBuilder = new StringBuilder();

        // Move this line outside the loop to get activityDesignationdata only once
        GroupActivityDesignation activityDesignationdata = groupActivityDesignationRepository.findByGroupId(id)
                .orElse(null);

        for (TaskCategory category : taskCategories) {
            Optional<GroupActivityDesignation> activityDesignation = groupActivityDesignationRepository
                    .findByGroupId(category.getId());

            if (activityDesignation.isPresent()) {
                String roleIds = activityDesignation.get().getRoleId();
                if (roleIds != null) {
                    if (roleIdsBuilder.length() > 0) {
                        roleIdsBuilder.append(",");
                    }
                    roleIdsBuilder.append(roleIds);
                }
            }
        }

        // Append activityDesignationdata's role ID outside the loop
        // if (activityDesignationdata != null) {
        // if (roleIdsBuilder.length() > 0) {
        // roleIdsBuilder.append(",");
        // }
        // roleIdsBuilder.append(activityDesignationdata.getRoleId());
        // }
        String finalRoleIds = roleIdsBuilder.toString();
        List<Roles> roles = rolesRepository.findAllRoles(finalRoleIds);
        if (activityDesignationdata != null) {
            List<Roles> additionalRoles = rolesRepository.findAllRolesparticular(activityDesignationdata.getRoleId());
            roles.addAll(additionalRoles);

        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Designation retrieved successfully", roles));
    }

    @Override
    public ResponseEntity<?> getTaskCategory() {
        int user_id = AuthUserData.getUserId();
        Users user = usersRepository.findByActiveEmployeeById(user_id).orElse(null);

        if (user != null) {
            Integer roleId = user.getRole_id().stream().findFirst().map(Roles::getId).orElse(null);
            if (roleId != null) {
                Integer group_id = groupActivityDesignationRepository.findGroupId(roleId);
                if (group_id != null) {

                    Optional<TaskCategory> taskCategory = taskCategoryRepository.getByIdisActive(group_id);

                    if (taskCategory.isPresent()) {
                        TaskCategoryResponse categoryResponse = new TaskCategoryResponse();
                        BeanUtils.copyProperties(taskCategory.get(), categoryResponse);
                        String[] categories = taskCategory.get().getCategory().split(",");
                        // categoriesArray = categories.split(",");

                        categoryResponse.setCategories(categories);

                        return ResponseEntity.status(HttpStatus.OK)
                                .body(new ApiResponse(true, "Task Activity retrieved successfully",
                                        categoryResponse));
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ApiResponse(false, "Task Activity not found for the given group ID",
                                        Collections.emptyList()));
                    }

                } else {
                    return ResponseEntity.status(HttpStatus.OK)
                            .body(new ApiResponse(false, "No Data Found", Collections.emptyList()));
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "User has no associated designation", Collections.emptyList()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> DeleteTheTaskCategory(List<Integer> id) {
        taskCategoryRepository.updateIsDeleted(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Activity Deleted Succesfully", null));
    }

}
