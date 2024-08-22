package pm.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.model.task.TaskCategory;

@Service
public interface TaskCategoryService {
    ResponseEntity<?> create(String groupName, String category);

    ResponseEntity<?> list(int page, int size, boolean search, String value);

    ResponseEntity<?> byId(int id);

    ResponseEntity<?> update(TaskCategory taskCategory, int id);

    ResponseEntity<?> groupMappingDesignation(int groupId, String roleId);

    ResponseEntity<?> getCategoriesroles(int id);

    ResponseEntity<?> getTaskCategory();
    
    ResponseEntity<?> DeleteTheTaskCategory(List<Integer> id);
}
