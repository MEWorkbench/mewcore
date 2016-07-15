package pt.uminho.ceb.biosystems.mew.core.sampling.abstraction;

import java.io.IOException;

public interface ISampling {

	public SamplingResult run() throws IOException;
}
