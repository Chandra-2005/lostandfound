package com.lostfound.lostfound.Repository;

import com.lostfound.lostfound.Model.Items;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends MongoRepository<Items, String> {
    List<Items> findByUserid(String userid);
    List<Items> findByTypeAndUseridNot(String type, String userid);
    List<Items> findByFoundByUserId(String finderId);
    List<Items> findByUseridAndType(String userid, String type);
}
