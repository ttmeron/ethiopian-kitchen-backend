package com.resturant.service;


import com.resturant.dto.SoftDrinkDTO;
import com.resturant.entity.SoftDrink;
import com.resturant.exception.ResourceNotFoundException;
import com.resturant.mapper.SoftDrinkMapper;
import com.resturant.repository.SoftDrinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SoftDrinkServiceImpl implements SoftDrinkService{

    @Autowired
    private SoftDrinkRepository softDrinkRepository;
    @Autowired
    private SoftDrinkMapper softDrinkMapper;

    @Override
    public List<SoftDrinkDTO> getAllSoftDrinks() {
        log.info("Fetching all active soft drinks");
        List<SoftDrink> softDrinks = softDrinkRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        return softDrinkMapper.toDtoList(softDrinks);

    }

    @Override
    public SoftDrinkDTO getSoftDrinkById(Long id) {
        log.info("Fetching soft drink with id: {}", id);
        SoftDrink softDrink = softDrinkRepository.findActiveById(id)
                .orElseThrow(() -> {
                    log.error("Soft drink not found with id: {}", id);
                    return new ResourceNotFoundException("Soft drink not found with id: " + id);
                });
        return softDrinkMapper.toDto(softDrink);
    }

    @Override
    public SoftDrinkDTO createSoftDrink(SoftDrinkDTO softDrinkDTO) {
        log.info("Creating new soft drink: {}", softDrinkDTO.getName());
        SoftDrink softDrink = softDrinkMapper.toEntity(softDrinkDTO);
        softDrink.setIsActive(true);
        SoftDrink savedDrink = softDrinkRepository.save(softDrink);
        log.info("Soft drink created successfully with id: {}", savedDrink.getId());
        return softDrinkMapper.toDto(savedDrink);
    }

    @Override
    public SoftDrinkDTO updateSoftDrink(Long id, SoftDrinkDTO softDrinkDTO) {
        log.info("Updating soft drink with id: {}", id);
        SoftDrink existingDrink = softDrinkRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Soft drink not found with id: {}", id);
                    return new ResourceNotFoundException("Soft drink not found with id: " + id);
                });

        softDrinkMapper.updateEntityFromDto(softDrinkDTO, existingDrink);
        SoftDrink updatedDrink = softDrinkRepository.save(existingDrink);
        log.info("Soft drink updated successfully with id: {}", id);
        return softDrinkMapper.toDto(updatedDrink);
    }

    @Override
    public void deleteSoftDrink(Long id) {
        log.info("Deleting soft drink with id: {}", id);
        SoftDrink drink = softDrinkRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Soft drink not found with id: {}", id);
                    return new ResourceNotFoundException("Soft drink not found with id: " + id);
                });

        drink.setIsActive(false);
        softDrinkRepository.save(drink);

        log.info("Soft drink marked as inactive with id: {}", id);

    }

    @Override
    public void activateSoftDrink(Long id) {
        log.info("Activating soft drink with id: {}", id);
        SoftDrink softDrink = softDrinkRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Soft drink not found with id: {}", id);
                    return new ResourceNotFoundException("Soft drink not found with id: " + id);
                });
        softDrink.setIsActive(true);
        softDrinkRepository.save(softDrink);
        log.info("Soft drink activated successfully with id: {}", id);

    }

    @Override
    public void deactivateSoftDrink(Long id) {
        log.info("Deactivating soft drink with id: {}", id);
        SoftDrink softDrink = softDrinkRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Soft drink not found with id: {}", id);
                    return new ResourceNotFoundException("Soft drink not found with id: " + id);
                });
        softDrink.setIsActive(false);
        softDrinkRepository.save(softDrink);
        log.info("Soft drink deactivated successfully with id: {}", id);

    }

}
