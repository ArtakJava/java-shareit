package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.PageParameter;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.messageManager.MessageHolder;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public RequestDto create(long userId, RequestDto requestDto) {
        Request request = RequestMapper.mapToRequestEntity(requestDto, getUser(userId));
        RequestDto result = RequestMapper.mapToRequestDto(requestRepository.save(request));
        log.info(MessageHolder.SUCCESS_CREATE, result);
        return result;
    }

    @Override
    public List<RequestDto> getOwnRequests(long userId) {
        User requestor = getUser(userId);
        List<RequestDto> requests = requestRepository.findAllByRequestorId(requestor.getId(), Sort.by("created")).stream()
                .map(RequestMapper::mapToRequestDto)
                .collect(Collectors.toList());
        Map<Long, List<Item>> itemsByRequest = findItemsByRequests(requests);
        log.info(MessageHolder.SUCCESS_GET_ALL);
        return RequestMapper.mapRequestsDtoWithItems(requests, itemsByRequest);
    }

    @Override
    public List<RequestDto> getAll(long userId, PageParameter pageParameter) {
        List<Request> requests;
        if (pageParameter.isPresent()) {
            requests = requestRepository.findAllByRequestorIdNot(
                    userId,
                    PageRequest.of(
                            pageParameter.getPage(),
                            pageParameter.getSize(),
                            Sort.by("created").descending())
            );
        } else {
            requests = requestRepository.findAllByRequestorIdNot(userId, Sort.by("created").descending());
        }
        List<RequestDto> requestsDto = requests.stream()
                .map(RequestMapper::mapToRequestDto)
                .collect(Collectors.toList());
        Map<Long, List<Item>> itemsByRequest = findItemsByRequests(requestsDto);
        log.info(MessageHolder.SUCCESS_GET_REQUESTS);
        return RequestMapper.mapRequestsDtoWithItems(requestsDto, itemsByRequest);
    }

    @Override
    public RequestDto get(long userId, long requestId) {
        User user = getUser(userId);
        RequestDto requestWithOutItems = RequestMapper.mapToRequestDto(requestRepository.getReferenceById(requestId));
        List<Item> itemsByRequest = findItemsByRequest(requestWithOutItems);
        RequestDto request = RequestMapper.mapRequestDtoWithItems(requestWithOutItems, itemsByRequest);
        log.info(String.format(MessageHolder.SUCCESS_GET), request);
        return request;
    }

    @Override
    public User getUser(long userId) {
        User result = new User();
        User user = userRepository.getReferenceById(userId);
        if (user.getName() != null) {
            result = user;
        }
        return result;
    }

    private Map<Long, List<Item>> findItemsByRequests(List<RequestDto> requests) {
        List<Item> items = itemRepository.findByRequestIdIn(requests.stream()
                .map(RequestDto::getId)
                .collect(Collectors.toSet()));
        return items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
    }

    private List<Item> findItemsByRequest(RequestDto request) {
        return itemRepository.findByRequestId(request.getId());
    }
}