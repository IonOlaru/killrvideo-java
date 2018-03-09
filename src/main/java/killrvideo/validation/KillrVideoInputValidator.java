package killrvideo.validation;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import killrvideo.comments.CommentsServiceOuterClass.CommentOnVideoRequest;
import killrvideo.comments.CommentsServiceOuterClass.GetUserCommentsRequest;
import killrvideo.comments.CommentsServiceOuterClass.GetVideoCommentsRequest;
import killrvideo.common.CommonTypes;
import killrvideo.ratings.RatingsServiceOuterClass.GetRatingRequest;
import killrvideo.ratings.RatingsServiceOuterClass.GetUserRatingRequest;
import killrvideo.ratings.RatingsServiceOuterClass.RateVideoRequest;
import killrvideo.search.SearchServiceOuterClass.GetQuerySuggestionsRequest;
import killrvideo.search.SearchServiceOuterClass.SearchVideosRequest;
import killrvideo.statistics.StatisticsServiceOuterClass.GetNumberOfPlaysRequest;
import killrvideo.statistics.StatisticsServiceOuterClass.RecordPlaybackStartedRequest;
import killrvideo.suggested_videos.SuggestedVideosService.GetRelatedVideosRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.CreateUserRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.GetUserProfileRequest;
import killrvideo.user_management.UserManagementServiceOuterClass.VerifyCredentialsRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetLatestVideoPreviewsRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetUserVideoPreviewsRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoPreviewsRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.GetVideoRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitUploadedVideoRequest;
import killrvideo.video_catalog.VideoCatalogServiceOuterClass.SubmitYouTubeVideoRequest;

/**
 * Manually validate inputs from Grpc clients
 *
 * The best practices require using Java Bean Validation (JSR-303)
 * but since the request objects are generated by Grpc, we cannot add
 * annotations on them, thus requiring manual and tedious validation.
 * See : https://groups.google.com/forum/#!topic/grpc-io/Q7fyXSA4jmM
 */
@Service
public class KillrVideoInputValidator {

    /**  Logger to class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(KillrVideoInputValidator.class);
    
    /**
     * Valid inputs.
     *
     * @param request
     *      current request
     * @param streamObserve
     *      Stream to publish error.
     * @return
     *      if valid
     */
    public boolean isValid(CommentOnVideoRequest request, StreamObserver<?> streamObserver) {
        StringBuilder errorMessage = initErrorString(request);
        boolean isValid = 
                  notEmpty(!request.hasUserId()    || isBlank(request.getUserId().getValue()),  "userId",  "video request",errorMessage) &&
                  notEmpty(!request.hasVideoId()   || isBlank(request.getVideoId().getValue()), "videoId", "video request",errorMessage) &&
                  notEmpty(!request.hasCommentId() || isBlank(request.getCommentId().getValue()), "commentId", "video request",errorMessage) &&
                  notEmpty(isBlank(request.getComment()), "comment", "video request",errorMessage);
        return validate(streamObserver, errorMessage, isValid);
    }

    /**
     * Valid inputs.
     *
     * @param request
     *      current request
     * @param streamObserve
     *      Stream to publish error.
     * @return
     *      if valid
     */
    public boolean isValid(GetUserCommentsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = 
                  notEmpty(!request.hasUserId() || isBlank(request.getUserId().getValue()),  "userId",  "comment request",errorMessage) &&
                  positive(request.getPageSize() <= 0,  "page size",  "comment request",errorMessage);
        return validate(streamObserver, errorMessage, isValid);
    }

    
    
    public boolean isValid(GetVideoCommentsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (!request.hasVideoId() || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for get video comment request\n");
            isValid = false;
        }

        if (request.getPageSize() <= 0) {
            errorMessage.append("\t\tpage size should be strictly positive for get video comment request");
            isValid = false;
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(RateVideoRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (!request.hasVideoId() || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for rate video request\n");
            isValid = false;
        }

        if (!request.hasUserId() || isBlank(request.getUserId().getValue())) {
            errorMessage.append("\t\tuser id should be provided for rate video request");
            isValid = false;
        }
        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(GetRatingRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (!request.hasVideoId() || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for get video rating request\n");
            isValid = false;
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(GetUserRatingRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (!request.hasVideoId() || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for get user rating request\n");
            isValid = false;
        }

        if (!request.hasUserId() || isBlank(request.getUserId().getValue())) {
            errorMessage.append("\t\tuser id should be provided for get user rating request\n");
            isValid = false;
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(SearchVideosRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (isBlank(request.getQuery())) {
            errorMessage.append("\t\tquery string should be provided for search videos request\n");
            isValid = false;
        }

        if (request.getPageSize() <= 0) {
            errorMessage.append("\t\tpage size should be strictly positive for search videos request\n");
            isValid = false;
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(GetQuerySuggestionsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (isBlank(request.getQuery())) {
            errorMessage.append("\t\tquery string should be provided for get video suggestions request\n");
            isValid = false;
        }

        if (request.getPageSize() <= 0) {
            errorMessage.append("\t\tpage size should be strictly positive for get video suggestions request\n");
            isValid = false;
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(RecordPlaybackStartedRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (request.getVideoId() == null || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for record playback started request\n");
            isValid = false;
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(GetNumberOfPlaysRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (request.getVideoIdsCount() <= 0) {
            errorMessage.append("\t\tvideo ids should be provided for get number of plays request\n");
            isValid = false;
        }

        if (request.getVideoIdsCount() > 20) {
            errorMessage.append("\t\tcannot do a get more than 20 videos at once for get number of plays request\n");
            isValid = false;
        }

        for (CommonTypes.Uuid uuid : request.getVideoIdsList()) {
            if (uuid == null || isBlank(uuid.getValue())) {
                errorMessage.append("\t\tprovided UUID values cannot be null or blank for get number of plays request\n");
                isValid = false;
            }
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(GetRelatedVideosRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (request.getVideoId() == null || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for get related videos request\n");
            isValid = false;
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(CreateUserRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (request.getUserId() == null || isBlank(request.getUserId().getValue())) {
            errorMessage.append("\t\tuser id should be provided for create user request\n");
            isValid = false;
        }

        if (isBlank(request.getPassword())) {
            errorMessage.append("\t\tpassword should be provided for create user request\n");
            isValid = false;
        }

        if (isBlank(request.getEmail())) {
            errorMessage.append("\t\temail should be provided for create user request\n");
            isValid = false;
        }
        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(VerifyCredentialsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (isBlank(request.getEmail())) {
            errorMessage.append("\t\temail should be provided for verify credentials request\n");
            isValid = false;
        }

        if (isBlank(request.getPassword())) {
            errorMessage.append("\t\tpassword should be provided for verify credentials request\n");
            isValid = false;
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(GetUserProfileRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;


        if (request.getUserIdsCount() > 20) {
            errorMessage.append("\t\tcannot get more than 20 user profiles at once for get user profile request\n");
            isValid = false;
        }

        for (CommonTypes.Uuid uuid : request.getUserIdsList()) {
            if (uuid == null || isBlank(uuid.getValue())) {
                errorMessage.append("\t\tprovided UUID values cannot be null or blank for get user profile request\n");
                isValid = false;
            }
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(SubmitUploadedVideoRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (request.getVideoId() == null || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for submit uploaded video request\n");
            isValid = false;
        }

        if (request.getUserId() == null || isBlank(request.getUserId().getValue())) {
            errorMessage.append("\t\tuser id should be provided for submit uploaded video request\n");
            isValid = false;
        }

        if (isBlank(request.getName())) {
            errorMessage.append("\t\tvideo name should be provided for submit uploaded video request\n");
            isValid = false;
        }

        if (isBlank(request.getDescription())) {
            errorMessage.append("\t\tvideo description should be provided for submit uploaded video request\n");
            isValid = false;
        }

        if (request.getTagsList() == null || CollectionUtils.isEmpty(request.getTagsList())) {
            errorMessage.append("\t\tvideo tags list should be provided for submit uploaded video request\n");
            isValid = false;
        }

        if (isBlank(request.getUploadUrl())) {
            errorMessage.append("\t\tvideo upload url should be provided for submit uploaded video request\n");
            isValid = false;
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(SubmitYouTubeVideoRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (request.getVideoId() == null || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for submit youtube video request\n");
            isValid = false;
        }

        if (request.getUserId() == null || isBlank(request.getUserId().getValue())) {
            errorMessage.append("\t\tuser id should be provided for submit youtube video request\n");
            isValid = false;
        }

        if (isBlank(request.getName())) {
            errorMessage.append("\t\tvideo name should be provided for submit youtube video request\n");
            isValid = false;
        }

        if (isBlank(request.getDescription())) {
            errorMessage.append("\t\tvideo description should be provided for submit youtube video request\n");
            isValid = false;
        }

        if (isBlank(request.getYouTubeVideoId())) {
            errorMessage.append("\t\tvideo youtube id should be provided for submit youtube video request\n");
            isValid = false;
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(GetVideoRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (request.getVideoId() == null || isBlank(request.getVideoId().getValue())) {
            errorMessage.append("\t\tvideo id should be provided for submit youtube video request\n");
            isValid = false;
        }
        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(GetVideoPreviewsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;


        if (request.getVideoIdsCount() >= 20) {
            errorMessage.append("\t\tcannot get more than 20 videos at once for get video previews request\n");
            isValid = false;
        }

        for (CommonTypes.Uuid uuid : request.getVideoIdsList()) {
            if (uuid == null || isBlank(uuid.getValue())) {
                errorMessage.append("\t\tprovided UUID values cannot be null or blank for get video previews request\n");
                isValid = false;
            }
        }

        return validate(streamObserver, errorMessage, isValid);
    }


    public boolean isValid(GetLatestVideoPreviewsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (request.getPageSize() <= 0) {
            errorMessage.append("\t\tpage size should be strictly positive for get latest preview video request\n");
            isValid = false;
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    public boolean isValid(GetUserVideoPreviewsRequest request, StreamObserver<?> streamObserver) {
        final StringBuilder errorMessage = initErrorString(request);
        boolean isValid = true;

        if (request.getUserId() == null || isBlank(request.getUserId().getValue())) {
            errorMessage.append("\t\tuser id should be provided for get user video previews request\n");
            isValid = false;
        }

        if (request.getPageSize() <= 0) {
            errorMessage.append("\t\tpage size should be strictly positive for get user video previews request\n");
            isValid = false;
        }

        return validate(streamObserver, errorMessage, isValid);
    }

    private StringBuilder initErrorString(Object request) {
        return new StringBuilder("Validation error for '" + request.toString() + "' : \n");
    }
    
   /**
    * Deduplicate condition evaluation
    * @param assertion
    *      current condition
    * @param fieldName
    *      fieldName to evaluate
    * @param request
    *      GRPC reauest
    * @param errorMessage
    *      concatenation of error messages
    * @return
    */
   private boolean notEmpty(boolean assertion, String fieldName, String request, StringBuilder errorMessage) {
       if (assertion) {
           errorMessage.append("\t\t");
           errorMessage.append(fieldName);
           errorMessage.append("should be provided for comment on ");
           errorMessage.append(request);
           errorMessage.append("\n");
       }
       return !assertion;
   }
   
   private boolean positive(boolean assertion, String fieldName, String request, StringBuilder errorMessage) {
       if (assertion) {
           errorMessage.append("\t\t");
           errorMessage.append(fieldName);
           errorMessage.append("should be strictly positive for ");
           errorMessage.append(request);
           errorMessage.append("\n");
       }
       return !assertion;
   }

    private boolean validate(StreamObserver<?> streamObserver, StringBuilder errorMessage, boolean isValid) {
        if (isValid) {
            return true;
        } else {
            final String description = errorMessage.toString();
            LOGGER.error(description);
            streamObserver.onError(Status.INVALID_ARGUMENT.withDescription(description).asRuntimeException());
            streamObserver.onCompleted();
            return false;
        }
    }

}
