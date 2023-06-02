package ru.practicum.shareit.request.dao;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequestorId(long userId, Sort sort);

    List<Request> findAllByRequestorIdNot(long userId, PageRequest created);

    List<Request> findAllByRequestorIdNot(long userId, Sort sort);
}