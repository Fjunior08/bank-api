package com.bank.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import com.bank.api.entity.Account;
import com.bank.api.exception.BusinessException;
import com.bank.api.repository.AccountRepository;
import com.bank.api.repository.TransactionRepository;
import com.bank.api.repository.IdempotencyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @InjectMocks
    private AccountService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        //importante pra não bloquear os testes
        when(idempotencyRepository.existsById(any())).thenReturn(false);
    }

    @Test
    void shouldCreateAccount() {
        Account acc = new Account();
        acc.setOwner("Franciss");

        when(accountRepository.save(any())).thenReturn(acc);

        var result = service.create("Franciss");

        assertEquals("Franciss", result.getOwner());
        verify(accountRepository, times(1)).save(any());
    }

    @Test
    void shouldDeposit() {
        Account acc = new Account();
        acc.setBalance(BigDecimal.ZERO);

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(acc);

        service.deposit(1L, BigDecimal.TEN, null);

        assertEquals(BigDecimal.TEN, acc.getBalance());
    }

    @Test
    void shouldNotDepositInvalidValue() {
        Account acc = new Account();
        acc.setBalance(BigDecimal.ZERO);

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(acc);

        assertThrows(BusinessException.class, () ->
                service.deposit(1L, BigDecimal.ZERO, null));
    }

    @Test
    void shouldWithdraw() {
        Account acc = new Account();
        acc.setBalance(BigDecimal.valueOf(100));

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(acc);

        service.withdraw(1L, BigDecimal.valueOf(50), null);

        assertEquals(BigDecimal.valueOf(50), acc.getBalance());
    }

    @Test
    void shouldNotWithdrawWithoutBalance() {
        Account acc = new Account();
        acc.setBalance(BigDecimal.TEN);

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(acc);

        assertThrows(BusinessException.class, () ->
                service.withdraw(1L, BigDecimal.valueOf(50), null));
    }

    @Test
    void shouldTransfer() {
        Account from = new Account();
        from.setBalance(BigDecimal.valueOf(100));

        Account to = new Account();
        to.setBalance(BigDecimal.ZERO);

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(from);
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(to);

        service.transfer(1L, 2L, BigDecimal.valueOf(50), null);

        assertEquals(BigDecimal.valueOf(50), from.getBalance());
        assertEquals(BigDecimal.valueOf(50), to.getBalance());
    }

    @Test
    void shouldNotTransferWithoutBalance() {
        Account from = new Account();
        from.setBalance(BigDecimal.TEN);

        Account to = new Account();
        to.setBalance(BigDecimal.ZERO);

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(from);
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(to);

        assertThrows(BusinessException.class, () ->
                service.transfer(1L, 2L, BigDecimal.valueOf(50), null));
    }
}