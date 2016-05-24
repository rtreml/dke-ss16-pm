package at.jku.dke.pm.domain;

import java.time.Duration;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Model {

	protected String processId;

	protected String footprint;

	protected int noCases = 0;

	// gesamtduration
	protected Duration minDuration;
	protected Duration maxDuration;
	protected Duration avgDuration;

	protected ProcessNet processNet;

	protected List<Case> cases;

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getFootprint() {
		return footprint;
	}

	public void setFootprint(String footprint) {
		this.footprint = footprint;
	}

	public int getNoCases() {
		return noCases;
	}

	public void setNoCases(int noCases) {
		this.noCases = noCases;
	}

	public Duration getMinDuration() {
		return minDuration;
	}

	public void setMinDuration(Duration minDuration) {
		this.minDuration = minDuration;
	}

	public Duration getMaxDuration() {
		return maxDuration;
	}

	public void setMaxDuration(Duration maxDuration) {
		this.maxDuration = maxDuration;
	}

	public Duration getAvgDuration() {
		return avgDuration;
	}

	public void setAvgDuration(Duration avgDuration) {
		this.avgDuration = avgDuration;
	}

	public ProcessNet getProcessNet() {
		return processNet;
	}

	public void setProcessNet(ProcessNet processNet) {
		this.processNet = processNet;
	}

	public List<Case> getCases() {
		return cases;
	}

	public void setCases(List<Case> cases) {
		this.cases = cases;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
