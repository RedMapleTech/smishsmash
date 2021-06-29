# Overview

Proof of concept Android application to inspect the currently stored SMS messages for possible SMS phishing (`smishing`) messages.

For more, read our [accompanying blog](https://redmaple.tech/blogs/smish-smash/).

# Inspection Approach

1. Gets all the messages currently in the SMS inbox.
2. Look for keywords in the message body that are uniquely attributed to an organisational target. e.g. "Revenue and Customs" for HMRC.
3. Check if the number is in the phone contacts.
4. Look for URLs in the message body.
5. Inspect the URL for keywords associated to an organisation (e.g. "tax", "rebate" for HMRC).
6. If we have associated the message to a possible organisation, compare the URL to what we'd expect from the organisation. For example, HMRC messages should come from `gov.uk` domains.
