package io.fundrequest.core.request.claim.handler;

import io.fundrequest.core.request.claim.dto.ClaimDto;
import io.fundrequest.core.request.claim.event.RequestClaimedEvent;
import io.fundrequest.core.request.claim.github.GithubSolverResolver;
import io.fundrequest.core.request.view.IssueInformationDto;
import io.fundrequest.core.request.view.RequestDto;
import io.fundrequest.core.request.view.RequestDtoMother;
import io.fundrequest.platform.github.CreateGithubComment;
import io.fundrequest.platform.github.GitHubCommentFactory;
import io.fundrequest.platform.github.GithubGateway;
import io.fundrequest.platform.github.parser.GithubIssueCommentsResult;
import io.fundrequest.platform.github.parser.GithubUser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class CreateGithubCommentOnClosedHandlerTest {

    private static final String SOLVER = "gdhfjghiuyutfyd";
    private static final String EXPECTED_MESSAGE = "ytufg";

    private CreateGithubCommentOnClosedHandler handler;

    private GithubGateway githubGateway;
    private GitHubCommentFactory gitHubCommentFactory;
    private GithubSolverResolver githubSolverResolver;
    private String githubUser = "ytruyt";

    @Before
    public void setUp() {
        githubGateway = mock(GithubGateway.class);
        gitHubCommentFactory = mock(GitHubCommentFactory.class);
        githubSolverResolver = mock(GithubSolverResolver.class);
        handler = new CreateGithubCommentOnClosedHandler(githubGateway, githubSolverResolver, gitHubCommentFactory, true, githubUser);
    }

    @Test
    public void ignoresGithubComment() {
        handler = new CreateGithubCommentOnClosedHandler(githubGateway, githubSolverResolver, gitHubCommentFactory, false, githubUser);

        handler.createGithubCommentOnRequestClaimed(mock((RequestClaimedEvent.class)));

        verifyZeroInteractions(githubGateway);
    }

    @Test
    public void postsGithubComment_lessThan2CommentsPresent() {
        final RequestClaimedEvent event = new RequestClaimedEvent("1324354", RequestDtoMother.freeCodeCampNoUserStories(), mock(ClaimDto.class), SOLVER, LocalDateTime.now());
        final String expectedMessage = EXPECTED_MESSAGE;
        final RequestDto request = event.getRequestDto();
        final IssueInformationDto issueInformation = request.getIssueInformation();
        final ArgumentCaptor<CreateGithubComment> createGithubCommentArgumentCaptor = ArgumentCaptor.forClass(CreateGithubComment.class);
        final List<GithubIssueCommentsResult> existingComments = Arrays.asList(createCommentFromGithubUserMock(0));

        when(githubGateway.getCommentsForIssue(issueInformation.getOwner(), issueInformation.getRepo(), issueInformation.getNumber())).thenReturn(existingComments);
        when(githubSolverResolver.solveResolver(request)).thenReturn(Optional.of(SOLVER));
        when(gitHubCommentFactory.createResolvedComment(request.getId(), SOLVER)).thenReturn(expectedMessage);

        handler.createGithubCommentOnRequestClaimed(event);

        verify(githubGateway).createCommentOnIssue(eq(issueInformation.getOwner()),
                                                   eq(issueInformation.getRepo()),
                                                   eq(issueInformation.getNumber()),
                                                   createGithubCommentArgumentCaptor.capture());
        assertThat(createGithubCommentArgumentCaptor.getValue().getBody()).isEqualTo(expectedMessage);
    }

    @Test
    public void postsGithubComment_2CommentsPresent() {
        final RequestClaimedEvent event = new RequestClaimedEvent("1324354", RequestDtoMother.freeCodeCampNoUserStories(), mock(ClaimDto.class), SOLVER, LocalDateTime.now());
        final RequestDto request = event.getRequestDto();
        final IssueInformationDto issueInformation = request.getIssueInformation();
        final ArgumentCaptor<CreateGithubComment> createGithubCommentArgumentCaptor = ArgumentCaptor.forClass(CreateGithubComment.class);
        final GithubIssueCommentsResult firstComment = createCommentFromGithubUserMock(-2);
        final GithubIssueCommentsResult secondComment = createCommentFromGithubUserMock(-1);
        final List<GithubIssueCommentsResult> existingComments = Arrays.asList(firstComment, secondComment);

        when(githubGateway.getCommentsForIssue(issueInformation.getOwner(), issueInformation.getRepo(), issueInformation.getNumber())).thenReturn(existingComments);
        when(githubSolverResolver.solveResolver(request)).thenReturn(Optional.of(SOLVER));
        when(gitHubCommentFactory.createResolvedComment(request.getId(), SOLVER)).thenReturn(EXPECTED_MESSAGE);

        handler.createGithubCommentOnRequestClaimed(event);

        verify(githubGateway).editCommentOnIssue(eq(issueInformation.getOwner()),
                                                 eq(issueInformation.getRepo()),
                                                 eq(secondComment.getId()),
                                                 createGithubCommentArgumentCaptor.capture());
        assertThat(createGithubCommentArgumentCaptor.getValue().getBody()).isEqualTo(EXPECTED_MESSAGE);
    }

    @Test
    public void postsGithubComment_moreThan2CommentsPresent() {
        final RequestClaimedEvent event = new RequestClaimedEvent("1324354", RequestDtoMother.freeCodeCampNoUserStories(), mock(ClaimDto.class), SOLVER, LocalDateTime.now());
        final String expectedMessage = EXPECTED_MESSAGE;
        final RequestDto request = event.getRequestDto();
        final IssueInformationDto issueInformation = request.getIssueInformation();
        final ArgumentCaptor<CreateGithubComment> createGithubCommentArgumentCaptor = ArgumentCaptor.forClass(CreateGithubComment.class);
        final GithubIssueCommentsResult firstComment = createCommentFromGithubUserMock(-3);
        final GithubIssueCommentsResult secondComment = createCommentFromGithubUserMock(-1);
        final GithubIssueCommentsResult thirdComment = createCommentFromGithubUserMock(-2);

        final List<GithubIssueCommentsResult> existingComments = Arrays.asList(firstComment, secondComment, thirdComment);
        when(githubGateway.getCommentsForIssue(issueInformation.getOwner(), issueInformation.getRepo(), issueInformation.getNumber())).thenReturn(existingComments);
        when(githubSolverResolver.solveResolver(request)).thenReturn(Optional.of(SOLVER));
        when(gitHubCommentFactory.createResolvedComment(request.getId(), SOLVER)).thenReturn(expectedMessage);

        handler.createGithubCommentOnRequestClaimed(event);

        verify(githubGateway).editCommentOnIssue(eq(issueInformation.getOwner()),
                                                 eq(issueInformation.getRepo()),
                                                 eq(secondComment.getId()),
                                                 createGithubCommentArgumentCaptor.capture());
        assertThat(createGithubCommentArgumentCaptor.getValue().getBody()).isEqualTo(expectedMessage);
    }

    @Test
    public void postsGithubComment_noSolverFound() {
        final RequestClaimedEvent event = new RequestClaimedEvent("1324354", RequestDtoMother.freeCodeCampNoUserStories(), mock(ClaimDto.class), SOLVER, LocalDateTime.now());
        final RequestDto request = event.getRequestDto();

        when(githubSolverResolver.solveResolver(request)).thenReturn(Optional.empty());

        try {
            handler.createGithubCommentOnRequestClaimed(event);
            fail("RuntimeException expected");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("No solver found for request " + request.getId());
        }
    }

    private GithubIssueCommentsResult createCommentFromGithubUserMock(final long createdOfsetDays) {
        return new GithubIssueCommentsResult(Math.abs(UUID.randomUUID().getMostSignificantBits()),
                                             GithubUser.builder().login(githubUser).build(),
                                             "hdfgjh",
                                             ZonedDateTime.now().plusDays(createdOfsetDays),
                                             "gdfhgjhhjlkj;lk");
    }
}
