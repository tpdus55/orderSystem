package com.example.orderSystem.member.controller;

import com.example.orderSystem.common.auth.JwtTokenFilter;
import com.example.orderSystem.common.auth.JwtTokenProvider;
import com.example.orderSystem.member.domain.Member;
import com.example.orderSystem.member.dtos.MemberCreateDto;
import com.example.orderSystem.member.dtos.MemberDetailDto;
import com.example.orderSystem.member.dtos.MemberListDto;
import com.example.orderSystem.member.dtos.MemberLoginDto;
import com.example.orderSystem.member.service.MemberService;
import io.jsonwebtoken.JwtParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

//    회원가입
    @PostMapping("/create")
    public ResponseEntity<?> save(@RequestBody MemberCreateDto dto){
        Member member = memberService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(member.getId());
    }

//    user로그인
    @PostMapping("/doLogin")
    public String userLogin(@RequestBody MemberLoginDto dto){
        Member member = memberService.login(dto);
        String token = jwtTokenProvider.createToken(member);
        return token;
    }


//    회원목록조회
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MemberListDto> findAll(){
        return memberService.findAll();
    }

//    내 정보조회
    @GetMapping("/myInfo")
    public ResponseEntity<?> myInfo(){
        MemberDetailDto dto = memberService.myInfo();
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

//    회원상세조회
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){
        MemberDetailDto dto = memberService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

}
