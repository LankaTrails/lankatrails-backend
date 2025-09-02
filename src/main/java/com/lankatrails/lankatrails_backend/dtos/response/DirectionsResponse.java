package com.lankatrails.lankatrails_backend.dtos.response;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectionsResponse {
    private String status;
    private List<Route> routes;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Route> getRoutes() { return routes; }
    public void setRoutes(List<Route> routes) { this.routes = routes; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {
        private List<Leg> legs;

        public List<Leg> getLegs() { return legs; }
        public void setLegs(List<Leg> legs) { this.legs = legs; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Leg {
        private DurationValue duration;

        public DurationValue getDuration() { return duration; }
        public void setDuration(DurationValue duration) { this.duration = duration; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DurationValue {
        @JsonProperty("value")
        private long value; // seconds

        public long getValue() { return value; }
        public void setValue(long value) { this.value = value; }
    }
}
