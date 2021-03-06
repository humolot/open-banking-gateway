#!/usr/bin/env bash

BRANCH="$TRAVIS_BRANCH"
if [[ -n "$TRAVIS_PULL_REQUEST_BRANCH" ]]; then
    BRANCH="$TRAVIS_PULL_REQUEST_BRANCH"
    echo "Pull request branch identified: $TRAVIS_PULL_REQUEST_BRANCH"
fi

echo "Sending test results to codecov using $BRANCH"

# Production code
bash <(curl -s https://codecov.io/bash) -s './last-module-codecoverage*' -F backend  -B "$BRANCH";
bash <(curl -s https://codecov.io/bash) -s './consent-ui*' -F frontend -B "$BRANCH";

# Example code
bash <(curl -s https://codecov.io/bash) -s './fintech-examples/fintech-last-module-codecoverage*' -F fintech  -B "$BRANCH";
bash <(curl -s https://codecov.io/bash) -s './fintech-examples/fintech-ui*' -F fintech -B "$BRANCH";
