package practice.jpaboard.board.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import practice.jpaboard.board.dto.BoardSearchCond;
import practice.jpaboard.board.entity.Board;
import practice.jpaboard.board.entity.QBoard;
import practice.jpaboard.member.entity.QMember;

import java.util.List;

import static practice.jpaboard.board.entity.QBoard.*;
import static practice.jpaboard.member.entity.QMember.member;

@RequiredArgsConstructor
public class QuerydslBoardRepositoryImpl implements QuerydslBoardRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Board> findBoardList(BoardSearchCond boardSearchCond, Pageable pageable) {

        List<Board> content = jpaQueryFactory
                .select(board)
                .from(board)
                .join(board.member, member).fetchJoin()
                .where(searchCond(boardSearchCond.getKind(), boardSearchCond.getInputText()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = findBoardListCount(boardSearchCond);

        return new PageImpl<>(content, pageable, total);
    }

    private long findBoardListCount(BoardSearchCond boardSearchCond) {
        List<Board> content = jpaQueryFactory
                .select(board)
                .from(board)
                .join(board.member, member)
                .where(searchCond(boardSearchCond.getKind(), boardSearchCond.getInputText()))
                .fetch();

        return content.size();
    }


    private BooleanExpression searchCond(String kind, String inputText) {
        if ((!StringUtils.hasText(kind) && !StringUtils.hasText(inputText))
                || (StringUtils.hasText(kind) && !StringUtils.hasText(inputText))) {
            return null;
        }

        if (kind.equals("boardTitle")) {
            return boardTitleEq(inputText);
        }

        return memberNicknameEq(inputText);
    }

    private BooleanExpression boardTitleEq(String boardTitle) {
        if (!StringUtils.hasText(boardTitle)) {
            return null;
        }

        return board.boardTitle.contains(boardTitle);
    }

    private BooleanExpression memberNicknameEq(String memberNickname) {
        if (!StringUtils.hasText(memberNickname)) {
            return null;
        }

        return board.member.memberNickname.contains(memberNickname);
    }
}