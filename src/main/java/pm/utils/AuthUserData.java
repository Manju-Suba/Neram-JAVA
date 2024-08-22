package pm.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import pm.service.security.UserDetailsImpl;

public class AuthUserData {

    // public static int getUserId() {
    // int userid;
    // Authentication authentication =
    // SecurityContextHolder.getContext().getAuthentication();
    // UserDetailsImpl userDetails = (UserDetailsImpl)
    // authentication.getPrincipal();
    // if (authentication != null && authentication.isAuthenticated()) {
    // userid = userDetails.getId();
    // } else {
    // userid = 0;
    // }
    // return userid;
    //
    // }
    //
    // public static List<String> getUserRole() {
    // List<String> roles;
    // Authentication authentication =
    // SecurityContextHolder.getContext().getAuthentication();
    // UserDetailsImpl userDetails = (UserDetailsImpl)
    // authentication.getPrincipal();
    // if (authentication != null && authentication.isAuthenticated()) {
    // roles = userDetails.getAuthorities().stream()
    // .map(item -> item.getAuthority())
    // .collect(Collectors.toList());
    // } else {
    // roles = new ArrayList<String>();
    // }
    // return roles;
    // }
    //
    // public static String getBranch() {
    //
    // String branch;
    // Authentication authentication =
    // SecurityContextHolder.getContext().getAuthentication();
    // UserDetailsImpl userDetails = (UserDetailsImpl)
    // authentication.getPrincipal();
    // if (authentication != null && authentication.isAuthenticated()) {
    // branch = userDetails.getBranch();
    // } else {
    // branch = null;
    // }
    // return branch;
    //
    // }
    //
    // public static String getDesignation() {
    //
    // String designation;
    // Authentication authentication =
    // SecurityContextHolder.getContext().getAuthentication();
    // UserDetailsImpl userDetails = (UserDetailsImpl)
    // authentication.getPrincipal();
    // if (authentication != null && authentication.isAuthenticated()) {
    // designation = userDetails.getDesignation();
    // } else {
    // designation = null;
    // }
    // return designation;
    //
    // }

    public static UserDetailsImpl getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles;

        if (authentication != null && authentication.isAuthenticated()) {
            roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return (UserDetailsImpl) authentication.getPrincipal();

        } else {
            return null;
        }
    }

    public static int getUserId() {
        UserDetailsImpl userDetails = getUserDetails();
        return userDetails != null ? userDetails.getId() : 0;
    }

    public static List<String> getUserRole() {
        UserDetailsImpl userDetails = getUserDetails();
        return userDetails != null ? userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList()) : new ArrayList<>();
    }

    public static String getBranch() {
        UserDetailsImpl userDetails = getUserDetails();
        return userDetails != null ? userDetails.getBranch() : null;
    }

    public static String getDesignation() {
        UserDetailsImpl userDetails = getUserDetails();
        return userDetails != null ? userDetails.getDesignation() : null;
    }

    public static String getEmpid() {
        UserDetailsImpl userDetails = getUserDetails();
        return userDetails != null ? userDetails.getUsername() : null;
    }

}
