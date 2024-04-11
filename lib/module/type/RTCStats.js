/**
 * Types related to WebRTC stats.
 *
 * @public
 */
export let RTCStats;
(function (_RTCStats) {
  let IceCandidatePairState = /*#__PURE__*/function (IceCandidatePairState) {
    IceCandidatePairState["STATE_FAILED"] = "STATE_FAILED";
    IceCandidatePairState["STATE_FROZEN"] = "STATE_FROZEN";
    IceCandidatePairState["STATE_IN_PROGRESS"] = "STATE_IN_PROGRESS";
    IceCandidatePairState["STATE_SUCCEEDED"] = "STATE_SUCCEEDED";
    IceCandidatePairState["STATE_WAITING"] = "STATE_WAITING";
    return IceCandidatePairState;
  }({});
  _RTCStats.IceCandidatePairState = IceCandidatePairState;
  /**
   * WebRTC stats report. Contains diagnostics information about
   * `RTCPeerConnection`s and summarizes data for an ongoing call.
   */
})(RTCStats || (RTCStats = {}));
//# sourceMappingURL=RTCStats.js.map