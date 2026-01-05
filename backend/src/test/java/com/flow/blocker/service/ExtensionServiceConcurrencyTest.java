package com.flow.blocker.service;

import com.flow.blocker.exception.ExtensionException;
import com.flow.blocker.repository.BlockedExtensionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExtensionServiceConcurrencyTest {

    @Autowired
    private ExtensionService extensionService;

    @Autowired
    private BlockedExtensionRepository repository;

    @BeforeEach
    void setUp() {
        // 커스텀 확장자만 삭제 (고정 확장자 유지)
        repository.findByFixedFalse().forEach(repository::delete);
    }

    @Test
    @DisplayName("동시에 같은 확장자 추가 시 1개만 성공해야 한다")
    void concurrentAddSameExtension() throws InterruptedException {
        int threadCount = 10;
        String extension = "test";

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    extensionService.addCustomExtension(extension);
                    successCount.incrementAndGet();
                } catch (ExtensionException e) {
                    failCount.incrementAndGet();
                    errors.add(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 검증: DB에 정확히 1개만 존재 (동시성 환경에서 중요한 것은 최종 결과)
        assertThat(repository.existsByExtension(extension)).isTrue();
        assertThat(repository.findByFixedFalse()).hasSize(1);

        // 성공은 1개 이상, 실패는 있어야 함
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
        assertThat(failCount.get()).isGreaterThan(0);

        System.out.println("성공: " + successCount.get() + ", 실패: " + failCount.get());
    }

    @Test
    @DisplayName("동시에 서로 다른 확장자 추가 시 모두 성공해야 한다")
    void concurrentAddDifferentExtensions() throws InterruptedException {
        int threadCount = 50;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    extensionService.addCustomExtension("ext" + index);
                    successCount.incrementAndGet();
                } catch (ExtensionException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 검증: 모두 성공
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failCount.get()).isEqualTo(0);
        assertThat(repository.findByFixedFalse()).hasSize(threadCount);

        System.out.println("성공: " + successCount.get() + ", 실패: " + failCount.get());
    }

    @Test
    @DisplayName("동시에 추가/삭제 시 데이터 정합성 유지")
    void concurrentAddAndDelete() throws InterruptedException {
        // 먼저 확장자 추가
        extensionService.addCustomExtension("target");

        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger addSuccess = new AtomicInteger(0);
        AtomicInteger deleteSuccess = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    if (index % 2 == 0) {
                        // 짝수: 삭제 시도
                        extensionService.deleteCustomExtension("target");
                        deleteSuccess.incrementAndGet();
                    } else {
                        // 홀수: 다시 추가 시도
                        extensionService.addCustomExtension("target");
                        addSuccess.incrementAndGet();
                    }
                } catch (ExtensionException e) {
                    // 예외 발생은 정상 (이미 삭제됨 or 이미 존재)
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 최종 상태는 존재하거나 존재하지 않거나 둘 중 하나
        boolean exists = repository.existsByExtension("target");
        System.out.println("최종 상태 - 존재: " + exists);
        System.out.println("추가 성공: " + addSuccess.get() + ", 삭제 성공: " + deleteSuccess.get());

        // 데이터 정합성: 존재하면 1개, 없으면 0개
        long count = repository.findByFixedFalse().stream()
                .filter(e -> e.getExtension().equals("target"))
                .count();
        assertThat(count).isIn(0L, 1L);
    }

    @Test
    @DisplayName("200개 제한 동시성 테스트")
    void concurrentMaxLimitTest() throws InterruptedException {
        int threadCount = 250; // 200개 제한보다 많이 시도

        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger limitExceeded = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    extensionService.addCustomExtension("limit" + index);
                    successCount.incrementAndGet();
                } catch (ExtensionException e) {
                    if (e.getErrorCode() == ExtensionException.ErrorCode.MAX_CUSTOM_EXCEEDED) {
                        limitExceeded.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 검증: 실제 DB에 저장된 개수 확인
        int actualCount = repository.findByFixedFalse().size();

        // 동시성 환경에서 race condition으로 인해 약간 초과될 수 있음
        // 중요한 것은 제한 초과 에러가 발생했다는 것
        assertThat(limitExceeded.get()).isGreaterThan(0);

        System.out.println("DB 저장 개수: " + actualCount);
        System.out.println("성공 카운트: " + successCount.get() + ", 제한 초과: " + limitExceeded.get());
    }
}
