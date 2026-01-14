package org.example.hooliguns.controller;

import org.example.hooliguns.dto.BoardEntryResponse;
import org.example.hooliguns.dto.HooliganCardResponse;
import org.example.hooliguns.service.BoardService;
import org.example.hooliguns.service.HooliganService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/hooligans")
public class HooliganController {
    private final HooliganService hooliganService;
    private final BoardService boardService;

    public HooliganController(HooliganService hooliganService,
                              BoardService boardService) {
        this.hooliganService = hooliganService;
        this.boardService = boardService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT','ADMIN','IMMORTAL')")
    public Mono<HooliganCardResponse> card(@PathVariable String id) {
        return hooliganService.card(id);
    }

    @GetMapping("/board")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT','ADMIN','IMMORTAL')")
    public Flux<BoardEntryResponse> board(@RequestParam(defaultValue = "3") int limit) {
        return boardService.board(limit);
    }
}
