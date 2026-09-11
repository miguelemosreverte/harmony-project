# Live workflow editor proof

Verified locally on 2026-09-10 against the authenticated network at `.artifacts/live-11365731111510052500/`. The browser constructed **Reader defined offer**, reference `home-17`, without editing Daml.

1. Renamed the workflow, moved buyer review before financing approval, selected the generated legacy adapter, and added/removed a third action.
2. Submitted duplicate step names. The server returned **Step names must be distinct**, displayed persistently while form values remained intact. Completing the separate private financing approval caused a real participant-state refresh; the unfinished composition values remained intact.
3. Corrected the step name and proposed the workflow. A DOM observer captured **Pending** before the response. The observed draft contained the chosen order, roles, parties, and generated action. No composition source existed yet.
4. Opened the buyer's provisioned session. It displayed the exact draft, acceptance control, and no private financing payload. Acceptance created the core instance and both source contracts. Only the buyer's first review was available.
5. Executed review as the buyer, then approval as the bank. Both sessions observed a complete process with `Review.Review` confirmed and `LegacyFinancing.Application` approved. Completed steps exposed no execution control. Reloading recovered the same completed workflow.

The editor uses labelled native inputs, selects, fieldsets, and buttons. At 390 pixels the form and result fit without document overflow. Programmatic focus reached the named input and its native focus outline was visible. A provisioned identity link opened in the same tab now reloads the session automatically; normal state refresh keeps unfinished editor input.

Evidence: `.artifacts/verification/16-browser.json`, `16-bank-state.json`, `16-buyer-state.json`, `16-composer-bank.png`, `16-composer-buyer-mobile.png`, and `16-composer-form-mobile.png`. Scala formatting and Scala.js compilation/linking pass. The underlying two authenticated golden scenarios passed in 16a; no ledger code changed in this increment.

The owned live network was stopped after verification. A process check showed only the existing book preview JVM. DAR input and the complete Chapter 8 walkthrough remain the next part of this PRD step.
