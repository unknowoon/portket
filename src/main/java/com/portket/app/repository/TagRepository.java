package com.portket.app.repository;

import com.portket.app.domain.Tag;
import com.portket.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String tagName);
    Optional<Tag> findByUserAndName(User user, String name);
    List<Tag> findAllByUser(User user);
}
