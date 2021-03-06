/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: * Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer. * Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.sirix.diff.algorithm.fmse;

import static com.google.common.base.Preconditions.checkNotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sirix.api.XdmNodeReadTrx;
import org.sirix.access.trx.node.AbstractVisitor;
import org.sirix.api.ResourceManager;
import org.sirix.api.visitor.VisitResultType;
import org.sirix.exception.SirixException;
import org.sirix.node.Kind;
import org.sirix.node.immutable.ImmutableElement;
import org.sirix.node.immutable.ImmutableText;

/**
 * Label visitor. Treats empty-elements as internal nodes.
 * 
 * @author Johannes Lichtenberger, University of Konstanz
 * 
 */
public final class LabelFMSEVisitor extends AbstractVisitor {

  /** {@link XdmNodeReadTrx} implementation. */
  private final XdmNodeReadTrx mRtx;

  /** For each node type: list of inner nodes. */
  private final Map<Kind, List<Long>> mLabels;

  /** For each node type: list of leaf nodes. */
  private final Map<Kind, List<Long>> mLeafLabels;

  /**
   * Constructor.
   * 
   * @param pSession {@link ResourceManager} implementation
   * @throws SirixException if setting up sirix fails
   */
  public LabelFMSEVisitor(final XdmNodeReadTrx pReadTrx) throws SirixException {
    mRtx = checkNotNull(pReadTrx);
    mLabels = new HashMap<>();
    mLeafLabels = new HashMap<>();
  }

  @Override
  public VisitResultType visit(final ImmutableElement pNode) {
    final long nodeKey = pNode.getNodeKey();
    mRtx.moveTo(nodeKey);
    for (int i = 0, nspCount = mRtx.getNamespaceCount(); i < nspCount; i++) {
      mRtx.moveToNamespace(i);
      addLeafLabel();
      mRtx.moveTo(nodeKey);
    }
    for (int i = 0, attCount = mRtx.getAttributeCount(); i < attCount; i++) {
      mRtx.moveToAttribute(i);
      addLeafLabel();
      mRtx.moveTo(nodeKey);
    }
    if (!mLabels.containsKey(pNode.getKind())) {
      mLabels.put(pNode.getKind(), new ArrayList<Long>());
    }
    mLabels.get(pNode.getKind()).add(pNode.getNodeKey());
    return VisitResultType.CONTINUE;
  }

  @Override
  public VisitResultType visit(final ImmutableText pNode) {
    mRtx.moveTo(pNode.getNodeKey());
    addLeafLabel();
    return VisitResultType.CONTINUE;
  }

  /**
   * Add leaf node label.
   */
  private void addLeafLabel() {
    final Kind nodeKind = mRtx.getKind();
    if (!mLeafLabels.containsKey(nodeKind)) {
      mLeafLabels.put(nodeKind, new ArrayList<Long>());
    }
    mLeafLabels.get(nodeKind).add(mRtx.getNodeKey());
  }

  /**
   * Get labels.
   * 
   * @return the Labels
   */
  public Map<Kind, List<Long>> getLabels() {
    return mLabels;
  }

  /**
   * Get leaf labels.
   * 
   * @return the leaf labels
   */
  public Map<Kind, List<Long>> getLeafLabels() {
    return mLeafLabels;
  }
}
