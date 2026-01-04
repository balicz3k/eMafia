package com.mafia.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mafia.databaseModels.User;
import com.mafia.dto.CastVoteRequest;
import com.mafia.dto.CastVoteResponse;
import com.mafia.dto.VoteResultDto;
import com.mafia.entities.Game;
import com.mafia.repositories.GameRepository;
import com.mafia.services.VotingSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VotingController.class)
public class VotingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VotingSessionService votingSessionService;

    @MockBean
    private GameRepository gameRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UUID gameId;
    private UUID sessionId;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");

        gameId = UUID.randomUUID();
        sessionId = UUID.randomUUID();

        // Mock authentication
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(testUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void castVote_ShouldReturnResponse() throws Exception {
        CastVoteRequest request = new CastVoteRequest();
        request.setVotingSessionId(sessionId);
        request.setTargetUserId(UUID.randomUUID());

        CastVoteResponse response = new CastVoteResponse();
        response.setSuccess(true);
        response.setMessage("Vote cast");

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(new Game()));
        when(votingSessionService.castVote(eq(sessionId), eq(testUser.getId()), eq(request.getTargetUserId())))
                .thenReturn(response);

        mockMvc.perform(post("/api/voting/{gameId}/vote", gameId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vote cast"));
    }

    @Test
    void getResults_ShouldReturnList() throws Exception {
        VoteResultDto result = new VoteResultDto();
        result.setTargetUserId(UUID.randomUUID());
        result.setVoteCount(5);

        when(votingSessionService.getResults(sessionId)).thenReturn(Collections.singletonList(result));

        mockMvc.perform(get("/api/voting/{gameId}/results", gameId)
                .param("sessionId", sessionId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].voteCount").value(5));
    }
}
