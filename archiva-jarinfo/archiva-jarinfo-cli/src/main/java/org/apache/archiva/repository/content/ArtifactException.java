package org.apache.archiva.repository.content;

import java.io.IOException;

public class ArtifactException extends IOException {
    private Throwable cause;
    
	public ArtifactException(String message, Throwable cause) {
		super(message);
		this.cause = cause;
	}

	public ArtifactException(String message) {
		super(message);
	}

    @Override
    public Throwable getCause()
    {
        return cause;
    }
}
