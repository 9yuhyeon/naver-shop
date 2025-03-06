package com.sparta.myselectshop.service;

import com.sparta.myselectshop.entity.Folder;
        import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FolderService {

    private final FolderRepository folderRepository;

    public void addFolders(List<String> folderNames, User user) {
        // 해당 User의 Folder 목록 중 인자로 전달한 names와 일치한 name을 가진 Folder만 가져옴
        List<Folder> existFolderList = folderRepository.findAllByUserAndNameIn(user, folderNames);

        // 중복되지 않는 경우 해당 Folder를 저장하기 위한 리스트
        List<Folder> folderList = new ArrayList<>();

        // 사용자가 전달한 Folder 이름과 기존 존재하는 Folder의 이름이 일치하지 않을 경우 Folder 생성 및 저장
        for (String folderName : folderNames) {
            if (!isExsistFolderName(folderName, existFolderList)) {
                 Folder folder = new Folder(folderName, user);
                 folderList.add(folder);
            } else {
                throw new IllegalArgumentException("이미 존재하는 폴더입니다.");
            }
        }

        // 폴더 이름이 중복 되지 않는 폴더만 저장
        folderRepository.saveAll(folderList);
    }

    // 사용자가 전달한 Folder 이름과 일치한 리스트 중 name 값이 일치한 Folder가 있는지 검증
    private boolean isExsistFolderName(String folderName, List<Folder> existFolderList) {
        for (Folder folder : existFolderList) {
            if (folderName.equals(folder.getName())) {
                return true;
            }
        }
        return false;
    }
}
