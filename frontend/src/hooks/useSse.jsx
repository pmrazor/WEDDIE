// hooks/useSSE.js
import { useState, useEffect } from "react";

const useSSE = (onMatchingRequest) => {
  const [connected, setConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!onMatchingRequest) return;
    const authStorage = localStorage.getItem("auth-storage");
    let accessToken = "";

    if (authStorage) {
      try {
        const authData = JSON.parse(authStorage);
        accessToken = authData.state.accessToken;
        console.log(
          "[SSE] Token loaded:",
          accessToken.substring(0, 10) + "..."
        );
      } catch (error) {
        console.error("[SSE] Token load error:", error);
        return;
      }
    }

    const abortController = new AbortController();

    const connectSSE = async () => {
      try {
        console.log("[SSE] Connecting to server...");
        const response = await fetch(
          "https://weddie.ssafy.me/api/notifications/subscribe",
          {
            method: "GET",
            headers: {
              Authorization: `Bearer ${accessToken}`,
              Accept: "text/event-stream",
              "Cache-Control": "no-cache",
              Connection: "keep-alive",
            },
            credentials: "include",
            signal: abortController.signal,
          }
        );

        console.log("[SSE] Got response:", response.status);

        if (!response.ok) {
          throw new Error(`SSE connection failed: ${response.status}`);
        }

        console.log("[SSE] Connection established");
        setConnected(true);
        setIsLoading(false);

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = "";

        try {
          while (true) {
            const { value, done } = await reader.read();
            if (done) {
              console.log("[SSE] Stream complete");
              break;
            }

            const chunk = decoder.decode(value, { stream: true });
            console.log("[SSE] Received raw chunk:", chunk);
            buffer += chunk;

            const lines = buffer.split("\n");
            buffer = lines.pop() || "";

            for (const line of lines) {
              if (line.trim() === "") continue;
              console.log("[SSE] Processing line:", line);

              if (line.startsWith("event:")) {
                const eventType = line.slice(6).trim();
                console.log("[SSE] Event type:", eventType);
                continue;
              }

              if (line.startsWith("data:")) {
                const data = line.slice(5).trim();
                console.log("[SSE] Data received:", data);

                if (data === "연결되었습니다!") {
                  console.log("[SSE] Connection confirmed");
                  continue;
                }

                try {
                  const parsedData = JSON.parse(data);
                  console.log("[SSE] Parsed data:", parsedData);

                  if (parsedData.notificationType === "COUPLE_MATCHING") {
                    console.log("[SSE] Couple matching notification received");
                    onMatchingRequest(parsedData);
                  }
                } catch (e) {
                  console.error("[SSE] Parse error:", e, "Raw data:", data);
                }
              }
            }
          }
        } catch (error) {
          console.error("[SSE] Read error:", error);
          throw error;
        }
      } catch (error) {
        if (error.name === "AbortError") {
          console.log("[SSE] Connection aborted");
        } else {
          console.error("[SSE] Connection error:", error);
          setConnected(false);
          setIsLoading(false);
        }
      }
    };

    console.log("[SSE] Starting connection...");
    connectSSE();

    return () => {
      console.log("[SSE] Cleaning up...");
      abortController.abort();
      setConnected(false);
      setIsLoading(true);
    };
  }, [onMatchingRequest]);

  return { connected, isLoading };
};

export default useSSE;
