---
title: smtpSettings
slug: /main-concepts/metadata-standard/schemas/email/smtpsettings
---

# SmtpSettings

*This schema defines the SMTP Settings for sending Email*

## Properties

- **`emailingEntity`** *(string)*: Emailing Entity. Default: `MetaMart`.
- **`supportUrl`** *(string)*: Support Url. Default: `https://slack.meta-mart.org`.
- **`enableSmtpServer`** *(boolean)*: If this is enable password will details will be shared on mail. Default: `False`.
- **`metaMartUrl`** *(string)*: Metamart Server Endpoint.
- **`senderMail`** *(string)*: Mail of the sender.
- **`serverEndpoint`** *(string)*: Smtp Server Endpoint.
- **`serverPort`** *(integer)*: Smtp Server Port.
- **`username`** *(string)*: Smtp Server Username.
- **`password`** *(string)*: Smtp Server Password.
- **`transportationStrategy`** *(string)*: Must be one of: `['SMTP', 'SMTPS', 'SMTP_TLS']`. Default: `SMTP`.


Documentation file automatically generated at 2023-10-27 13:55:46.343512.
