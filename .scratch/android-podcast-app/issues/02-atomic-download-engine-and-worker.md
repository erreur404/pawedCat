# 02-atomic-download-engine-and-worker

Type: task
Status: open
Blocked by: 01

## Question

How to implement an OkHttp + WorkManager download engine that guarantees zero corrupted files by writing to a temporary `.part` file, verifying complete HTTP streaming / Content-Length / non-empty streams before an atomic file rename, supporting cancellation, and enforcing configurable network constraints (Wi-Fi only vs Cellular)?
