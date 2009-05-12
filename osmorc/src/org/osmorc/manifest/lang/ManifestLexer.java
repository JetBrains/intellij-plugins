/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.manifest.lang;

import com.intellij.lexer.LexerBase;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;
import org.osmorc.manifest.lang.headerparser.HeaderParserRepository;

/**
 * Author: Robert F. Beeger (robert@beeger.net)
 */
public class ManifestLexer extends LexerBase
{
  public ManifestLexer()
  {
    _headerParserRepository = ServiceManager.getService(HeaderParserRepository.class);
  }

  public void start(CharSequence buffer, int startOffset, int endOffset, int initialState)
  {
    _buffer = buffer;
    _endOffset = endOffset;
    _currentState = initialState;

    _tokenStart = startOffset;
    parseNextToken();
  }

  public void advance()
  {
    _tokenStart = _tokenEnd;
    parseNextToken();
  }

  public int getState()
  {
    return _currentState;
  }

  @Nullable
  public IElementType getTokenType()
  {
    return _tokenType;
  }

  public int getTokenStart()
  {
    return _tokenStart;
  }

  public int getTokenEnd()
  {
    return _tokenEnd;
  }

  public int getBufferEnd()
  {
    return _endOffset;
  }

  public CharSequence getBufferSequence()
  {
    return _buffer;
  }

  @Deprecated
  public void start(char[] buffer, int startOffset, int endOffset, int initialState)
  {
    throw new UnsupportedOperationException();
  }

  @Deprecated
  public char[] getBuffer()
  {
    throw new UnsupportedOperationException();
  }

  private void parseNextToken()
  {
    if (_tokenStart < _endOffset)
    {
      if (Character.isWhitespace(_buffer.charAt(_tokenStart)))
      {
        _tokenType = TokenType.WHITE_SPACE;
        _tokenEnd = _tokenStart + 1;
      }
      else if (_currentState == WAITING_FOR_HEADER_ASSIGNMENT_STATE)
      {
        if (_buffer.charAt(_tokenStart) == ':')
        {
          _tokenType = ManifestTokenTypes.HEADER_ASSIGNMENT;
        }
        else
        {
          _tokenType = TokenType.BAD_CHARACTER;
        }
        _tokenEnd = _tokenStart + 1;
        _currentState = _stateAfterColon;
      }
      else if ((_tokenStart == 0 || _buffer.charAt(_tokenStart - 1) == '\n') &&
          !Character.isWhitespace(_buffer.charAt(_tokenStart)))
      {
        if (_buffer.charAt(_tokenStart) == ':')
        {
          _tokenType = TokenType.BAD_CHARACTER;
          _tokenEnd = _tokenStart + 1;
          _currentState = SIMPLE_HEADER_STATE;
        }
        else
        {
          _tokenEnd = _tokenStart + 1;
          while (_tokenEnd < _endOffset && _buffer.charAt(_tokenEnd) != ':' &&
              !Character.isWhitespace(_buffer.charAt(_tokenEnd)))
          {
            _tokenEnd++;
          }
          _tokenType = ManifestTokenTypes.HEADER_NAME;
          _stateAfterColon =
              _headerParserRepository.getHeaderParser(_buffer.subSequence(_tokenStart, _tokenEnd).toString())
                  .isSimpleHeader()
                  ? SIMPLE_HEADER_STATE
                  : COMPLEX_HEADER_STATE;
          _currentState = WAITING_FOR_HEADER_ASSIGNMENT_STATE;
        }
      }
      else if (_currentState == COMPLEX_HEADER_STATE)
      {
        if ((_tokenStart + 1) < _endOffset &&
            _buffer.charAt(_tokenStart) == ':' && _buffer.charAt(_tokenStart + 1) == '=')
        {
          _tokenType = ManifestTokenTypes.DIRECTIVE_ASSIGNMENT;
          _tokenEnd = _tokenStart + 2;
        }
        else if (_buffer.charAt(_tokenStart) == '=')
        {
          _tokenType = ManifestTokenTypes.ATTRIBUTE_ASSIGNMENT;
          _tokenEnd = _tokenStart + 1;
        }
        else if (_buffer.charAt(_tokenStart) == ',')
        {
          _tokenType = ManifestTokenTypes.CLAUSE_SEPARATOR;
          _tokenEnd = _tokenStart + 1;
        }
        else if (_buffer.charAt(_tokenStart) == ';')
        {
          _tokenType = ManifestTokenTypes.PARAMETER_SEPARATOR;
          _tokenEnd = _tokenStart + 1;
        }
        else if (_buffer.charAt(_tokenStart) == '\"')
        {
          _tokenEnd = _tokenStart + 1;
          while (_tokenEnd < _endOffset && _buffer.charAt(_tokenEnd) != '\"')
          {
            _tokenEnd++;
          }
          if (_tokenEnd < _endOffset && _buffer.charAt(_tokenEnd) == '\"')
          {
            _tokenEnd++;
          }
          _tokenType = ManifestTokenTypes.HEADER_VALUE;
        }
        else
        {
          while (_tokenEnd < _endOffset &&
              _buffer.charAt(_tokenEnd) != '\"' &&
              _buffer.charAt(_tokenEnd) != ',' &&
              _buffer.charAt(_tokenEnd) != ';' &&
              _buffer.charAt(_tokenEnd) != '=' &&
              !(_tokenEnd + 1 < _endOffset &&
                  ((_buffer.charAt(_tokenEnd) == ':' && _buffer.charAt(_tokenEnd + 1) == '=') ||
                      (_buffer.charAt(_tokenEnd) == '\n' && !Character.isWhitespace(_buffer.charAt(_tokenEnd + 1))))) &&
              !(_tokenEnd + 1 == _endOffset && _buffer.charAt(_tokenEnd) == '\n'))
          {
            _tokenEnd++;
          }
          _tokenType = ManifestTokenTypes.HEADER_VALUE;
          if (_tokenEnd < _endOffset)
          {
            if (_buffer.charAt(_tokenEnd) == '=')
            {
              _tokenType = ManifestTokenTypes.ATTRIBUTE_NAME;
            }
            else if ((_tokenEnd + 1 < _endOffset) && _buffer.charAt(_tokenEnd) == ':' &&
                _buffer.charAt(_tokenEnd + 1) == '=')
            {
              _tokenType = ManifestTokenTypes.DIRECTIVE_NAME;
            }
          }
        }
      }
      else
      {
        _tokenEnd = _tokenStart + 1;
        while (_tokenEnd < _endOffset &&
            !(_buffer.charAt(_tokenEnd) == '\n' &&
                (_tokenEnd + 1 == _endOffset ||
                    !Character.isWhitespace(_buffer.charAt(_tokenEnd + 1)))))
        {
          _tokenEnd++;
        }
        _tokenType = ManifestTokenTypes.HEADER_VALUE;
      }
    }
    else
    {
      _tokenType = null;
      _tokenEnd = _tokenStart;
    }
  }

  private CharSequence _buffer;
  private int _endOffset;
  private int _tokenStart;
  private int _tokenEnd;
  private int _currentState;
  private int _stateAfterColon;
  private IElementType _tokenType;
  private static final int INITIAL_STATE = 0;
  private static final int SIMPLE_HEADER_STATE = 1;
  private static final int COMPLEX_HEADER_STATE = 2;
  private static final int WAITING_FOR_HEADER_ASSIGNMENT_STATE = 3;

  private final HeaderParserRepository _headerParserRepository;
}
