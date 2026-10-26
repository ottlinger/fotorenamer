/*
 * Copyright 2026, Aiki IT, FotoRenamer
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.aikiit.fotorenamer.image;

/**
 * Immutable batch result (or progress snapshot), without localized UI text.
 * Total counts detected image candidates; processed is renamed + skipped + failed.
 * Cancelled batches can have unprocessed candidates. Completed moves are not rolled back.
 */
public record RenameResult(int total, int processed, int renamed, int skipped, int failed, boolean cancelled) {
    public RenameResult {
        if (total < 0 || processed < 0 || renamed < 0 || skipped < 0 || failed < 0
                || processed > total || (long) renamed + skipped + failed != processed) {
            throw new IllegalArgumentException("Inconsistent rename counts");
        }
    }
}
