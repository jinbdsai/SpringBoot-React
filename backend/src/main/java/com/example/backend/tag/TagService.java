package com.example.backend.tag;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<Tag> findAll() {
        return tagRepository.findAllByOrderByNameAsc();
    }

    public List<Object[]> findPopular() {
        return tagRepository.findPopular();
    }

    /**
     * 태그 이름 리스트를 받아 없는 건 새로 만들고, 있는 건 재사용해서 Set 으로 반환.
     */
    @Transactional
    public Set<Tag> resolveOrCreate(List<String> names) {
        Set<Tag> result = new HashSet<>();
        if (names == null) return result;
        for (String raw : names) {
            String name = normalize(raw);
            if (name.isEmpty()) continue;
            Tag tag = tagRepository.findByName(name).orElseGet(() -> tagRepository.save(new Tag(name)));
            result.add(tag);
        }
        return result;
    }

    private String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.startsWith("#")) s = s.substring(1);
        s = s.replaceAll("\\s+", "_").toLowerCase();
        return s.length() > 50 ? s.substring(0, 50) : s;
    }
}
