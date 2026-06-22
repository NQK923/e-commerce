package com.learnfirebase.ecommerce.order.adapter.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.learnfirebase.ecommerce.common.application.pagination.PageRequest;
import com.learnfirebase.ecommerce.common.application.pagination.PageResponse;
import com.learnfirebase.ecommerce.order.adapter.in.web.AdminOutboxController;
import com.learnfirebase.ecommerce.order.application.dto.OutboxEventDto;
import com.learnfirebase.ecommerce.order.application.port.in.QueryFailedOutboxEventsUseCase;
import com.learnfirebase.ecommerce.order.application.port.in.RetryOutboxEventUseCase;

import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class AdminOutboxControllerTest {

    @Mock
    private QueryFailedOutboxEventsUseCase queryFailedOutboxEventsUseCase;

    @Mock
    private RetryOutboxEventUseCase retryOutboxEventUseCase;

    @Test
    void getFailedEventsQueriesUseCaseCorrectly() {
        AdminOutboxController controller = new AdminOutboxController(queryFailedOutboxEventsUseCase, retryOutboxEventUseCase);
        
        PageResponse<OutboxEventDto> expectedResponse = PageResponse.<OutboxEventDto>builder()
                .content(Collections.emptyList())
                .page(0)
                .size(10)
                .totalElements(0)
                .totalPages(0)
                .build();
                
        when(queryFailedOutboxEventsUseCase.queryFailedEvents(any(PageRequest.class))).thenReturn(expectedResponse);

        ResponseEntity<PageResponse<OutboxEventDto>> response = controller.getFailedEvents(2, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());

        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(queryFailedOutboxEventsUseCase).queryFailedEvents(pageRequestCaptor.capture());
        
        PageRequest pageRequest = pageRequestCaptor.getValue();
        assertEquals(2, pageRequest.getPage());
        assertEquals(20, pageRequest.getSize());
    }

    @Test
    void retryEventInvokesRetryUseCase() {
        AdminOutboxController controller = new AdminOutboxController(queryFailedOutboxEventsUseCase, retryOutboxEventUseCase);

        ResponseEntity<Void> response = controller.retryEvent("event-uuid-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(retryOutboxEventUseCase).retryEvent("event-uuid-123");
    }
}
