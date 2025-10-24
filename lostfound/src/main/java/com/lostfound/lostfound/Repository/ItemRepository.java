package com.lostfound.lostfound.Repository;

import com.lostfound.lostfound.Model.Items;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ItemRepository extends MongoRepository<Items, String> {
}
