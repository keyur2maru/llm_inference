// IEsperWhisperService.aidl
package io.esper.whisper;

interface IEsperWhisperService {
    String transcribeAudio(String filePath);
}