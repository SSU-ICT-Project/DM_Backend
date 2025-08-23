package com.dm.dmbackend.domain.goal.subGoal.controller;

import com.dm.dmbackend.domain.account.auth.loginUser.LoginUser;
import com.dm.dmbackend.domain.account.auth.loginUser.LoginUserDto;
import com.dm.dmbackend.domain.goal.subGoal.dto.req.SubGoalRequest;
import com.dm.dmbackend.domain.goal.subGoal.service.SubGoalService;
import com.dm.dmbackend.global.common.response.ApiResponse;
import com.dm.dmbackend.global.exception.ReturnCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="/rest-api/v1/subGoal")
@RequiredArgsConstructor
@Tag(name = "Sub Goal", description = "하위목표 API")
public class ApiV1SubGoalController {
    private final SubGoalService subGoalService;

    // 하위목표 생성
    @PostMapping
    @Operation(summary = "하위목표 생성")
    public ApiResponse<String> addSubGoal(@RequestBody @Valid SubGoalRequest subGoalRequest, @LoginUser LoginUserDto loginUser){
        subGoalService.addSubGoal(subGoalRequest, loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 하위목표 수정
    @PutMapping("/{subGoalId}")
    @Operation(summary = "하위목표 수정")
    public ApiResponse<String> updateSubGoal(@PathVariable("subGoalId") Long subGoalId,
                                             @RequestBody @Valid SubGoalRequest subGoalRequest, @LoginUser LoginUserDto loginUser){
        subGoalService.updateSubGoal(subGoalId, subGoalRequest, loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }

    // 하위목표 삭제
    @DeleteMapping("/{subGoalId}")
    @Operation(summary = "하위목표 삭제")
    public ApiResponse<String> deleteSubGoal(@PathVariable("subGoalId") Long subGoalId, @LoginUser LoginUserDto loginUser){
        subGoalService.deleteSubGoal(subGoalId, loginUser);
        return ApiResponse.of(ReturnCode.SUCCESS);
    }
}
