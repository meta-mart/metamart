/*
 *  Copyright 2023 DigiTrans.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import { act } from '@testing-library/react';
import { renderHook } from '@testing-library/react-hooks';
import { useClipboard } from './useClipBoard';

const clipboardWriteTextMock = jest.fn();
const clipboardMock = {
  writeText: clipboardWriteTextMock,
};

Object.defineProperty(window.navigator, 'clipboard', {
  value: clipboardMock,
  writable: true,
});

const value = 'Test Value';
const callBack = jest.fn();
const timeout = 1000;

describe('useClipboard hook', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('Should copy to clipboard', async () => {
    clipboardWriteTextMock.mockResolvedValue(value);
    const { result } = renderHook(() => useClipboard(value, timeout, callBack));

    await act(async () => {
      result.current.onCopyToClipBoard();
    });

    expect(navigator.clipboard.writeText).toHaveBeenCalledWith(value);
    expect(result.current.hasCopied).toBe(true);
    expect(callBack).toHaveBeenCalled();
  });

  it('Should handle error while copying to clipboard', async () => {
    clipboardWriteTextMock.mockRejectedValue('Error');
    const { result } = renderHook(() => useClipboard(value));

    await act(async () => {
      result.current.onCopyToClipBoard();
    });

    expect(navigator.clipboard.writeText).toHaveBeenCalledWith(value);
    expect(result.current.hasCopied).toBe(false);
  });

  it('Should reset hasCopied after the timeout', async () => {
    clipboardWriteTextMock.mockResolvedValue(value);

    jest.useFakeTimers();

    const { result, rerender } = renderHook(
      ({ value, timeout, callBack }) => useClipboard(value, timeout, callBack),
      { initialProps: { value, timeout, callBack } }
    );

    await act(async () => {
      result.current.onCopyToClipBoard();
    });

    expect(result.current.hasCopied).toBe(true);

    jest.advanceTimersByTime(timeout);

    rerender({ value, timeout, callBack });

    expect(result.current.hasCopied).toBe(false);
  });
});
