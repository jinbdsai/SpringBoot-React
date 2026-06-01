package com.example.backend.tag;

import com.example.backend.tag.dao.TagDao;
import com.example.backend.tag.dto.PopularTagResponseDTO;
import com.example.backend.tag.dto.TagDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private final TagDao tagDao;
    private final TagRepository tagRepository;

    public TagServiceImpl(TagDao tagDao, TagRepository tagRepository) {
        this.tagDao = tagDao;
        this.tagRepository = tagRepository;
    }

    @Override
    public List<TagDTO> findAll() {
        return tagDao.selectAllOrderByName().stream()
                .map(TagDTO::from)
                .toList();
    }

    @Override
    public List<PopularTagResponseDTO> findPopular() {
        return tagDao.selectPopular().stream()
                .map(PopularTagResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional
    public Set<Tag> resolveOrCreate(List<String> names) {
        Set<Tag> result = new HashSet<>();
        if (names == null) return result;
        for (String raw : names) {
            String name = normalize(raw);
            if (name.isEmpty()) continue;
            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(new Tag(name)));
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
