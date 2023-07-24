package com.example.messenger.consumer.api;

import com.example.messenger.consumer.api.model.ListenerContainerResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/v1/admin/listener")
public interface AdminListenerApi {
  @GetMapping
  ResponseEntity<List<ListenerContainerResponseDto>> getAllListenerContainers();

  @PostMapping("/{listenerId}/start")
  ResponseEntity<Void> startListener(@PathVariable String listenerId);

  @PostMapping("/{listenerId}/stop")
  ResponseEntity<Void> stopListener(@PathVariable String listenerId);

  @PostMapping("/{listenerId}/pause")
  ResponseEntity<Void> pauseListener(@PathVariable String listenerId);

  @PostMapping("/{listenerId}/resume")
  ResponseEntity<Void> resumeListener(@PathVariable String listenerId);
}
