package com.dm.dmbackend.domain.goal.mainGoal.controller;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUser;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.goal.mainGoal.dto.req.MainGoalRequest;
import com.dm.dmbackend.domain.goal.mainGoal.dto.res.MainGoalWithSubGoalsResponse;
import com.dm.dmbackend.domain.goal.mainGoal.entity.MainGoalPage;
import com.dm.dmbackend.domain.goal.mainGoal.service.MainGoalService;
import com.dm.dmbackend.global.common.response.ApiResponse;
import com.dm.dmbackend.global.common.response.DMPage;
import com.dm.dmbackend.global.exception.ReturnCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="/rest-api/v1/mainGoal")
@RequiredArgsConstructor
@Tag(name = "Main Goal", description = "상위목표 API")
public class ApiV1MainGoalController {
    private final MainGoalService mainGoalService;

    // 상위목표 생성
    @PostMapping
    @Operation(summary = "상위목표 생성")
    public ApiResponse<String> addMainGoal(@RequestBody @Valid MainGoalRequest mainGoalRequest, @LoginUser LoginUserDto loginUser){
        mainGoalService.addMainGoal(mainGoalRequest, loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 목표 리스트 조회
    @GetMapping
    @Operation(summary = "목표 리스트 조회")
    public ApiResponse<MainGoalWithSubGoalsResponse> getAllGoal(@ModelAttribute MainGoalPage mainGoalPage, @LoginUser LoginUserDto loginUser){
        Pageable pageable = PageRequest.of(mainGoalPage.getPage(), mainGoalPage.getSize());
        return ApiResponse.of(DMPage.of(mainGoalService.getAllGoal(pageable, loginUser)));
    }

    // 상위목표 수정
    @PutMapping("/{mainGoalId}")
    @Operation(summary = "상위목표 수정")
    public ApiResponse<String> updateMainGoal(@PathVariable("mainGoalId") Long mainGoalId,
                                          @RequestBody @Valid MainGoalRequest mainGoalRequest, @LoginUser LoginUserDto loginUser){
        mainGoalService.updateMainGoal(mainGoalId, mainGoalRequest, loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 상위목표 삭제
    @DeleteMapping("/{mainGoalId}")
    @Operation(summary = "상위목표 삭제")
    public ApiResponse<String> deleteMainGoal(@PathVariable("mainGoalId") Long mainGoalId, @LoginUser LoginUserDto loginUser){
        mainGoalService.deleteMainGoal(mainGoalId, loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }
}
