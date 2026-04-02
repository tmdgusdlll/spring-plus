package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.example.expert.domain.todo.dto.request.TodoSearchRequest;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

public class TodoCustomRepositoryImpl implements TodoCustomRepository{

    private final JPAQueryFactory queryFactory;

    public TodoCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(todo)
                        .leftJoin(todo.user, user)
                        .fetchJoin()
                        .where(todo.id.eq(todoId))
                        .fetchOne()
        );
    }

    @Override
    public Page<TodoSearchResponse> searchTodoByMultiCondition(TodoSearchRequest request, Pageable pageable) {

        // 1. 실제 데이터 값
        List<TodoSearchResponse> result = queryFactory
                .select(Projections.constructor(TodoSearchResponse.class,
                        todo.title, manager.count(), comment.count()
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .leftJoin(todo.comments, comment)
                .where(
                        titleContains(request.getTitle()),
                        createdAtAfter(request.getStartDate()),
                        createdBefore(request.getEndDate()),
                        nicknameContains(request.getNickname())
                )
                .groupBy(todo.id)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 데이터 개수
        Long total = queryFactory
                .select(todo.count())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .leftJoin(todo.comments, comment)
                .where(
                        titleContains(request.getTitle()),
                        createdAtAfter(request.getStartDate()),
                        createdBefore(request.getEndDate()),
                        nicknameContains(request.getNickname())
                )
                .fetchOne();

        // 3. total이 null일 경우 NPE 방지
        if (total == null) {
            total = 0L;
        }

        // 4. Page 객체로 전환
        return new PageImpl<>(result, pageable, total);
    }

    // 제목 검색조건
    private BooleanExpression titleContains(String title) {
        return title != null ? todo.title.contains(title) : null;
    }
    // 생성일 검색조건 (기간의 시작)
    private BooleanExpression createdAtAfter(LocalDateTime startDate) {
        return startDate != null ? todo.createdAt.after(startDate) : null;
    }
    // 생성일 검색조건 (기간의 끝)
    private BooleanExpression createdBefore(LocalDateTime endDate) {
        return endDate != null ? todo.createdAt.before(endDate) : null;
    }
    // 닉네임 검색조건
    private BooleanExpression nicknameContains(String nickname) {
        return nickname != null ? manager.user.nickname.contains(nickname) : null;
    }
}
