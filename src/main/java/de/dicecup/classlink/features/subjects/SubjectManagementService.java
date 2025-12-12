package de.dicecup.classlink.features.subjects;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubjectManagementService {

    private final SubjectRepository subjectRepository;

    @Transactional
    public Subject createSubject(String name, String description) {

        if(userIsNotAdmin()){
            throw new IllegalStateException("Only admins can create subjects");
        }

        Subject subject = new Subject();
        subject.setName(name);
        subject.setDescription(description);
        return subjectRepository.save(subject);
    }

    @Transactional
    public Subject updateSubject(UUID subjectID, String name, String description){

        if(userIsNotAdmin()){
            throw new IllegalStateException("Only admins can update subjects");
        }
        subjectRepository.findById(subjectID).ifPresent(subject -> {
            subject.setName(name);
            subject.setDescription(description);
            subjectRepository.save(subject);
        });
        return subjectRepository.findById(subjectID).orElseThrow();
    }

    private boolean userIsNotAdmin(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream().noneMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN"));
    }
}
