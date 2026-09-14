# Harmonia architecture — version 2

Status: preparing a correction to version 1 for joint review.
Recorded: 2026-09-14 UTC.
Branch: `design/architecture-clarity-v2`.
Input: [01-structure.png](01-structure.png), first committed at `eca791d`.
Source evidence and first-generation history: [design.md](design.md).

---

## 1. Review and intent

Miguel authorized a new version after discussing substantive issues in version 1:

> Just make sure that whatever you do, you are happy and you comment everything. I will be able to see it with you, alongside you.

Keep the two architectural views together, preserve the approved visual language,
and correct the relationships before presenting the revision. Version 1 remains
available in its original tab; version 2 gets a separate URL and new tab. This is
an architectural design review, not a use-case demonstration or implementation audit.

---

## 2. Corrections anchored to the originals

| Observation in version 1 | Source meaning | Required correction |
| --- | --- | --- |
| Direct and generated routes form a chain | The original has separate “implements” and “binds” relations, followed by “both paths → one Core-managed model”. [A] | Draw two independent branches from the Binding DAR declaration to two vertically arranged application packages. |
| Binding DAR resembles a central service | It carries applicability and describes “which step kind a choice fulfils”. [B] | Reduce it to a flat declaration artifact. Keep Core and application packages as the main visual anchors. |
| Node text repeats connector labels | The direct path uses a new DAR; the generated path uses an existing unchanged DAR. [A] | Nodes identify the artifact. Connectors state how it participates. |
| Atomicity mentions only authorization | Core supports it “where the workflow structure and required authorizations allow it”. [C] | Retain bounded scope and both conditions. |
| Continuation is outside the Core grouping | The original marks Continuation as “«template» harmonia-core” and links it to the next definition. [D] | Extend the Core grouping to contain Continuation and Next definition. |
| Applicability links are implicit | Its original relations are “qualifies” and “selects choice”. [E] | Label these thin declaration relations without giving them the weight of execution arrows. |

[A]: https://github.com/miguelemosreverte/harmony-project/blob/4d38e1a/docs/proposal/harmonia-architecture.html#L249-L271
[B]: https://github.com/miguelemosreverte/harmony-project/blob/4d38e1a/docs/proposal/harmonia-architecture.html#L230-L250
[C]: https://github.com/miguelemosreverte/harmony-project/blob/4d38e1a/docs/proposal/harmonia.md#L66-L74
[D]: https://github.com/miguelemosreverte/harmony-project/blob/4d38e1a/docs/proposal/harmonia-architecture.html#L384-L392
[E]: https://github.com/miguelemosreverte/harmony-project/blob/4d38e1a/docs/proposal/harmonia-architecture.html#L473-L483

The original component relations are also explicit in [lines 300–311](https://github.com/miguelemosreverte/harmony-project/blob/4d38e1a/docs/proposal/harmonia-architecture.html#L300-L311).
The package import direction and final binding mechanism remain open. The new
lines describe declared participation, not resolved code imports or network calls.

---

## 3. Visual decisions

Keep the title, upper component view and lower contract view in the same places.
Stack the two application packages vertically so their independence is spatially
obvious. Connect each directly to the smaller declaration artifact. Give each line
its own clear label and enough space to avoid crossing another node.

Remove the large backing card and shadow from Binding DAR. It remains a named
Daml artifact inside the ledger boundary, with Builder's dashed generation arrow
ending on it. It is not an off-ledger service or runtime proxy.

The lower view preserves its main line. The Core enclosure expands downward to
include the continuation contracts. Applicability remains a secondary annotation.
Use labels to explain the relations and remove duplicated phrases from nodes.

---

## 4. Exact edit prompt

Input image: version 1, `01-structure.png`. This is an edit of that image.

```text
Edit this Harmonia architecture image into version 2. Preserve the COMPLETE landscape sheet, same dimensions, same two views, title, white/pale-blue/navy palette, typography, restrained dimensional pictograms and generous margins. Do not introduce scenarios, extra panels or an unrelated redesign. Correct the following structural issues carefully.

UPPER VIEW: replace the current horizontal chain Binding DAR — Direct — Generated binding with TWO independent relationships. Keep Application DARs as one enclosing area on the right, with its shared line "Own authority · ownership · visibility". Within it, place the two application packages VERTICALLY, one above the other, with equal size and visual weight:
TOP package: title "Direct"; supporting line "New application DAR".
BOTTOM package: title "Generated"; supporting line "Existing DAR · unchanged".
There must be NO connector between these two packages. Neither route depends on or produces the other.

Reduce the Binding DAR in the middle to a SMALL FLAT DOCUMENT, with a modest document glyph and title "Binding DAR". Remove its large rounded service-like card, shadow and duplicate "Application participation" subtitle. The declaration should take much less visual space than Core or the combined applications, while remaining readable. Keep it inside the on-ledger boundary and keep Builder's thin dashed "Generates binding + code" arrow terminating on this declaration. Keep the simple line between Core and the declaration labeled "declares participation".

From the RIGHT EDGE of the Binding DAR declaration draw TWO distinct blue relation lines, each ending at the LEFT EDGE of exactly one application package. The upper line goes only to Direct and is labeled "implements interfaces". The lower line goes only to Generated and is labeled "bound by generated code". Use independent right-angle paths if needed. Reserve generous horizontal clearance for those labels; shift and resize internal elements to make them fit. Neither line crosses, enters, exits, touches or passes behind the other application's card. No line between Direct and Generated. These lines express participation relationships, not import direction, so omit arrowheads on these two relations. Keep both fully visible.

In Harmonia Core, preserve "Workflow state · step rules". Replace "Bounded atomicity, when authorized" with two short supporting lines: "Bounded atomic blocks" and "Require compatible flow + authority". Preserve readable font size; this corrects a missing condition from the original proposal. Dapp, Ledger API, the ledger boundary, Canton host, Builder and DAR-source note keep their meanings and positions.
Remove the wide backing bar behind the quiet "harmonia-references · reference packages" note if needed to give the vertically arranged packages room. Keep that note small at the bottom of the upper view, with the original source credit. No new explanatory paragraphs.

LOWER VIEW: the Continuation and Next definition documents currently sit outside the Core workflow model enclosure. Extend that SAME pale-blue Core enclosure downward to include BOTH documents, while keeping it separate from Step interface, Applicability and the Application enclosure. The definition, instance, assigned step, continuation and next definition all belong inside Core. Preserve the uninterrupted Instance → Continuation → Next definition arrows. Preserve Role → Party as text INSIDE Instance. Replace the human avatar on Assigned step with a modest document/checklist glyph so it reads as a contract assignment rather than another participant.

Keep the main Definition → Instance → Assigned step → Step interface → Application choice line and its existing creates / assigns / exercises via labels. Its execution arrow must still reach Application choice, never the authority key. Keep App authority required and Own signatories · observers INSIDE the Application boundary.
On the two thin dotted Applicability annotation lines, add only these small labels: "qualifies" toward Step interface, and "selects choice" toward Application choice. Place these labels in clear whitespace beside their respective lines, away from the key and all card text. These dotted relations describe declarations, not additional executable steps.

Preserve the two original source credits and the bottom line "Still open: package imports · binding mechanism · interface placement". Avoid duplicated wording, dense fields, legend panels, new UI controls, decorative imagery and extra prose. Pay particular attention to connector endpoints and enclosure membership. The goal is a clean architectural relationship map whose routes and responsibilities are unambiguous. All text and all artwork must remain uncropped.
```

---

## 5. Generated artifacts and inspection

Pending. Record actual generation times, saved artifacts, exact prompts, visual
inspection and any further correction. A successful tool call does not establish
that the relationships are right. Inspect both views before selecting the version
to open for review.

---

## 6. Miguel's review of version 2

Pending. Append the actual feedback, artifact and commit reviewed, date recorded
and resulting decision. The authorization to revise version 1 is not an approval
of this candidate.
