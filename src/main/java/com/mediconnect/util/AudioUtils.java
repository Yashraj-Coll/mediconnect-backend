package com.mediconnect.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.springframework.stereotype.Component;

@Component
public class AudioUtils {
    
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    
    /**
     * Convert audio file to required format for transcription API
     * (16kHz, 16-bit PCM, mono)
     */
    public static Path convertToRequiredFormat(Path inputFile) throws IOException {
        try {
            // Check if conversion is needed
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(inputFile.toFile());
            AudioFormat sourceFormat = inputStream.getFormat();
            
            if (isRequiredFormat(sourceFormat)) {
                inputStream.close();
                return inputFile; // No conversion needed
            }
            
            // Define target format
            AudioFormat targetFormat = new AudioFormat(
                    SAMPLE_RATE,
                    SAMPLE_SIZE_IN_BITS,
                    CHANNELS,
                    SIGNED,
                    BIG_ENDIAN);
            
            // Convert to target format
            AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, inputStream);
            
            // Create output file
            Path outputFile = Paths.get(inputFile.getParent().toString(), 
                    "converted_" + inputFile.getFileName().toString().replace(".wav", "") + ".wav");
            
            // Write converted audio to output file
            AudioSystem.write(convertedStream, AudioFileFormat.Type.WAVE, outputFile.toFile());
            
            // Close streams
            convertedStream.close();
            inputStream.close();
            
            return outputFile;
            
        } catch (UnsupportedAudioFileException e) {
            throw new IOException("Unsupported audio format: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if audio format meets requirements for transcription API
     */
    private static boolean isRequiredFormat(AudioFormat format) {
        return Math.abs(format.getSampleRate() - SAMPLE_RATE) < 0.01 && 
               format.getSampleSizeInBits() == SAMPLE_SIZE_IN_BITS &&
               format.getChannels() == CHANNELS;
    }
}