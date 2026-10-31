package com.patentlens.service;

import com.patentlens.model.DetectedRejection;
import com.patentlens.model.RejectionAnalysis;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Mock LLM service providing deterministic, highly realistic USPTO patent rejection explanations
 * and targeted claim amendments. Ensures seamless offline demos without requiring API keys or network.
 */
@Service
public class MockLlmService implements LlmService {

    @Override
    public RejectionAnalysis explainAndSuggest(String officeActionText, List<DetectedRejection> detectedRejections, String patentDraftText) {
        RejectionAnalysis analysis = new RejectionAnalysis();
        analysis.setUsedMock(true);

        for (DetectedRejection rej : detectedRejections) {
            String ground = rej.getGround() != null ? rej.getGround() : "";
            List<String> citedArt = rej.getCitedPriorArt();
            String artCitation = !citedArt.isEmpty() ? String.join(", ", citedArt) : "the cited prior art";

            if (ground.contains("101")) {
                rej.setPlainReason(
                        "The examiner rejected the claim under 35 U.S.C. § 101 because organizing and scheduling parcel delivery is considered a fundamental economic practice or abstract mathematical/organizational concept under Step 2A of the Alice/Mayo framework. Reciting generic computer components (e.g., 'a server', 'a drone') without specific technological improvements fails to provide 'significantly more' under Step 2B."
                );
                rej.setSuggestedAmendment(
                        "Amend the claim to tie the dispatch logic directly to physical sensor hardware and deterministic hardware failsafes, e.g.:\n" +
                        "\"5. (Amended) A computer-implemented method for failsafe goods delivery using an unmanned aerial vehicle, comprising:\n" +
                        "receiving, by a cryptographic hardware security module within an onboard flight controller, an authenticated delivery manifest and destination coordinates;\n" +
                        "sampling environmental telemetry at 100 Hz using co-located optical time-of-flight sensors;\n" +
                        "synthesizing obstacle bounding volumes in a deterministic local coordinate frame; and\n" +
                        "initiating a mechanical latch release of a package payload only when the bounding volume beneath the landing gear is certified void of animate entities for a continuous duration of 1.5 seconds.\""
                );
                rej.setOvercomingArgument(
                        "The amendment transforms the abstract scheduling concept into a concrete, machine-specific physical safety control loop. Under MPEP 2106.05(a), integrating continuous physical sensor sampling (100 Hz optical ToF) and hardware-level actuation thresholds provides an inventive concept that improves the functioning of the robotic vehicle itself, successfully traversing Alice Step 2B."
                );
            } else if (ground.contains("102")) {
                rej.setPlainReason(
                        "The examiner contends that every element of the claim is expressly disclosed in a single prior art reference (" + artCitation + "). Specifically, " + artCitation + " teaches an aerial vehicle with rotors, GPS, and standard waypoint route planning to drop parcels. To overcome a § 102 anticipation rejection, the claim must include at least one novel structural limitation not taught in the reference."
                );
                rej.setSuggestedAmendment(
                        "Narrow the claim from generic GPS waypoint navigation to a specific dual-sensor fusion and pneumatic emergency response architecture:\n" +
                        "\"1. (Amended) A system for autonomous package delivery, comprising:\n" +
                        "an unmanned aerial vehicle (UAV) having a plurality of counter-rotating propulsion rotors;\n" +
                        "a dual-frequency global positioning system (GPS) receiver;\n" +
                        "a time-of-flight LiDAR sensor coupled with an optical stereo camera; and\n" +
                        "an onboard embedded flight controller configured to execute real-time Kalman sensor fusion combining said LiDAR sensor and said optical stereo camera to dynamically detect dynamic terrain hazards, and, upon detecting an obstacle within a 3-meter threshold during descent, automatically engage a redundant pneumatic ballistic parachute tether while cutting motor drive within 45 milliseconds to discharge a payload safely.\""
                );
                rej.setOvercomingArgument(
                        artCitation + " fails to disclose or suggest the combination of real-time Kalman sensor fusion combining optical stereo imaging and LiDAR, nor does it teach the specific sub-45ms ballistic parachute deployment threshold. Because anticipation requires a single prior art reference to disclose every single element arranged as claimed, these newly added technical limitations render the claim novel under 35 U.S.C. § 102."
                );
            } else if (ground.contains("103")) {
                rej.setPlainReason(
                        "The examiner combined multiple references (" + artCitation + ") to argue that a Person Having Ordinary Skill in the Art (POSITA) would have found it obvious to combine ground obstacle detection with autonomous UAV flight to avoid landing collisions."
                );
                rej.setSuggestedAmendment(
                        "Amend the claim to introduce a specific non-obvious synergistic technical structure:\n" +
                        "\"3. (Amended) The system of claim 1, further comprising a downward-facing stereoscopic camera array configured to map ground landing topography into a multi-layer octree voxel grid, wherein the onboard flight controller evaluates said multi-layer octree voxel grid to identify level ground clearances and commands the UAV to transition into an emergency hover mode until the ground landing obstacle clears.\""
                );
                rej.setOvercomingArgument(
                        "Neither cited reference teaches or suggests constructing a multi-layer octree voxel grid in real time on an embedded UAV controller to resolve dynamic touchdown clearances. The combination cited by the examiner relies on improper hindsight reconstruction. Under the Graham v. John Deere framework, there is no teaching, suggestion, or motivation (TSM) in the cited art to implement volumetric octree mapping on resource-constrained flight hardware."
                );
            } else if (ground.contains("112")) {
                rej.setPlainReason(
                        "The examiner found the claim indefinite under 35 U.S.C. § 112(b) for lack of antecedent basis. In patent drafting, whenever an element is introduced with a definite article ('said' or 'the'), it must have been explicitly introduced earlier in the claim set with an indefinite article ('a' or 'an')."
                );
                rej.setSuggestedAmendment(
                        "Ensure exact terminological alignment between parent and dependent claims:\n" +
                        "\"3. (Amended) The system of claim 1, further comprising an optical sensor array configured to identify a ground landing obstacle.\n" +
                        "4. (Amended) The system of claim 3, wherein said optical sensor array causes the UAV to hover until said ground landing obstacle clears.\""
                );
                rej.setOvercomingArgument(
                        "The amendment corrects the typographical singular/plural discrepancy in Claim 3 by introducing 'a ground landing obstacle'. Consequently, Claim 4's reference to 'said ground landing obstacle' possesses clear and unambiguous antecedent basis, satisfying the definiteness requirement of 35 U.S.C. § 112(b)."
                );
            } else {
                rej.setPlainReason(
                        "The examiner raised formal or substantive objections regarding patentability requirements based on the cited reference (" + artCitation + ")."
                );
                rej.setSuggestedAmendment(
                        "Amend the claim to add explicit operational boundaries and distinct structural features distinguishing your apparatus from " + artCitation + "."
                );
                rej.setOvercomingArgument(
                        "The proposed amendments introduce distinct physical limitations not disclosed or suggested by the examiner's citations, restoring patentability."
                );
            }
        }

        analysis.setDetectedRejections(detectedRejections);
        analysis.setOverallStrategy(
                "Recommended Response Strategy:\n" +
                "1. Amend independent Claim 1 to incorporate specific sensor fusion (LiDAR + Stereo Camera) and pneumatic failsafe mechanisms to overcome § 102 anticipation against Smith et al.\n" +
                "2. Narrow dependent Claim 3 with octree voxel grid topographic mapping to traverse the § 103 obviousness combination.\n" +
                "3. Align singular/plural terminology between Claims 3 and 4 to cure the § 112 antecedent basis rejection.\n" +
                "4. Restructure method Claim 5 around cryptographic hardware security verification and physical sensor polling to satisfy Alice Step 2B under § 101."
        );

        return analysis;
    }
}
