package com.sethhaskellcondie.thegamepensiveapi.domain;

import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionFailedDbValidation;
import com.sethhaskellcondie.thegamepensiveapi.exceptions.ExceptionResourceNotFound;

import java.util.List;

public abstract class EntityServiceImpl<T extends Entity<RequestDto, ResponseDto>, RequestDto, ResponseDto> implements EntityService<T, RequestDto, ResponseDto> {

    private final EntityRepository<T, RequestDto, ResponseDto> repository;

    public EntityServiceImpl(EntityRepository<T, RequestDto, ResponseDto> repository) {
        this.repository = repository;
    }

    @Override
    public List<T> getWithFilters(String filters) {
        return repository.getWithFilters(filters);
    }

    @Override
    public T getById(int id) throws ExceptionResourceNotFound {
        return repository.getById(id);
    }

    @Override
    public T createNew(T t) throws ExceptionFailedDbValidation {
        return repository.insert(t);
    }

    @Override
    public T updateExisting(T t) throws ExceptionFailedDbValidation {
        return repository.update(t);
    }

    @Override
    public void deleteById(int id) throws ExceptionResourceNotFound {
        repository.deleteById(id);
    }
}
