package com.lab.sample.service;

import com.lab.sample.dto.CustomerRequest;
import com.lab.sample.entity.Customer;
import com.lab.sample.exception.DuplicateResourceException;
import com.lab.sample.exception.ResourceNotFoundException;
import com.lab.sample.mapper.CustomerMapper;
import com.lab.sample.repository.CustomerRepository;
import com.lab.sample.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl service;

    private static Customer customer(Long id, String email) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setEmail(email);
        return customer;
    }

    @Test
    void create_withExistingEmail_throwsDuplicate() {
        when(customerRepository.existsByEmail("a@test.com")).thenReturn(true);
        CustomerRequest request = new CustomerRequest("Klinik", "a@test.com", null);

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(DuplicateResourceException.class);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void update_withUnknownId_throwsNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        CustomerRequest request = new CustomerRequest("Klinik", "a@test.com", null);

        assertThatThrownBy(() -> service.update(1L, request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_withEmailOfAnotherCustomer_throwsDuplicate() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer(1L, "old@test.com")));
        when(customerRepository.findByEmail("new@test.com")).thenReturn(Optional.of(customer(2L, "new@test.com")));
        CustomerRequest request = new CustomerRequest("Klinik", "new@test.com", null);

        assertThatThrownBy(() -> service.update(1L, request)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void update_withOwnEmail_succeeds() {
        Customer existing = customer(1L, "same@test.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.findByEmail("same@test.com")).thenReturn(Optional.of(existing));
        CustomerRequest request = new CustomerRequest("Klinik", "same@test.com", null);

        service.update(1L, request);

        verify(customerMapper).updateEntity(request, existing);
    }

    @Test
    void get_withUnknownId_throwsNotFound() {
        when(customerRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(9L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
