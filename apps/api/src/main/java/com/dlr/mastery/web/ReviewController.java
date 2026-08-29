package com.dlr.mastery.web;

import com.dlr.mastery.application.ReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewService.ReviewItem> pending() {
        return reviewService.pending();
    }

    @GetMapping("/today")
    public List<ReviewService.ReviewItem> today() {
        return reviewService.today();
    }

    @PostMapping("/{id}/complete")
    public ReviewService.ReviewCompletion complete(
            @PathVariable UUID id,
            @RequestBody ReviewRequest request
    ) {
        return reviewService.complete(id, request.successful());
    }

    public record ReviewRequest(boolean successful) {
    }
}
