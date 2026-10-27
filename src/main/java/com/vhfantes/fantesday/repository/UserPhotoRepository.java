package com.vhfantes.fantesday.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vhfantes.fantesday.entity.UserPhoto;

public interface UserPhotoRepository extends JpaRepository<UserPhoto, Long> {

}
