package com.portket.app.finder;

import com.portket.app.domain.Tag;
import com.portket.app.domain.User;
import com.portket.app.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagFinder {
    private final TagRepository tagRepository;

    public Tag getByNameOrThrow(String name) {
        return tagRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + name));
    }

    public Tag validOwner(String name, User user) {
        return tagRepository.findByUserAndName(user, name)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + name));
    }
}
