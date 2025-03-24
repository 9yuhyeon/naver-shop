package com.sparta.myselectshop.repository;

import com.sparta.myselectshop.entity.Folder;
import com.sparta.myselectshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    // 해당 User가 가진 전체 folder 목록 중 인자로 전달받은 names와 일치한 name을 가진 Folder를 List로 반환
    // select * from folder where user_id = ? and name in (?, ?, ?);
    List<Folder> findAllByUserAndNameIn(User user, Collection<String> names);

    List<Folder> findAllByUser(User user);
}
