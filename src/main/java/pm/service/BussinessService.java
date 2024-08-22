package pm.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.model.product.BussinessCategory;

@Service
public interface BussinessService {

    ResponseEntity<?> create(String name);

    ResponseEntity<?> list(int page, int size, boolean search, String value);

    ResponseEntity<?> byId(int id);

    ResponseEntity<?> update(String name, int id);
    ResponseEntity<?> deleteTheBussinessCategory(List<Integer> id);
}
