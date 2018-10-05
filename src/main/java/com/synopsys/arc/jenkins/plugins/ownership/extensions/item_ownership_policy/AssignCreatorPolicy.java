/*
 * The MIT License
 *
 * Copyright 2014 Oleg Nenashev, Synopsys Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.synopsys.arc.jenkins.plugins.ownership.extensions.item_ownership_policy;

import com.synopsys.arc.jenkins.plugins.ownership.Messages;
import com.synopsys.arc.jenkins.plugins.ownership.OwnershipDescription;
import com.synopsys.arc.jenkins.plugins.ownership.extensions.ItemOwnershipPolicy;
import com.synopsys.arc.jenkins.plugins.ownership.extensions.ItemOwnershipPolicyDescriptor;
import com.synopsys.arc.jenkins.plugins.ownership.util.AbstractOwnershipHelper;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.User;
import org.jenkinsci.plugins.ownership.model.OwnershipHelperLocator;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.stapler.DataBoundConstructor;
import java.util.Set;
import java.util.HashSet;

/**
 * A policy, which sets the item creator as an owner.
 * @author Oleg Nenashev
 * @since 0.5
 */
public class AssignCreatorPolicy extends ItemOwnershipPolicy {

    @DataBoundConstructor
    public AssignCreatorPolicy() {
    }

    @Override
    public OwnershipDescription onCreated(Item item) {

        AbstractOwnershipHelper<Item> helper = OwnershipHelperLocator.locate(item);

        if (helper == null) {
            return null;
        }

        if (item.getClass() == WorkflowJob.class && item.getParent().getClass() == WorkflowMultiBranchProject.class) {
            return helper.getOwnershipDescription(item);
        }
        else {

            User creator = User.current();
            Set<String> secondaryOwners = new HashSet<>();
            OwnershipDescription description = helper.getOwnershipDescription(item);

            if (creator == null || creator == User.getUnknown()) {
                return helper.getOwnershipDescription(item);
            }

            secondaryOwners = (description.getSecondaryOwnerIds());
            if (!description.getPrimaryOwnerId().equals(creator.getId()) &&
                    !description.getPrimaryOwnerId().equals(User.getUnknown().getId())) {
                secondaryOwners.add(description.getPrimaryOwnerId());
            }

            return new OwnershipDescription(true, creator.getId(), secondaryOwners);
        }
    }
    
    @Extension
    public static class DescriptorImpl extends ItemOwnershipPolicyDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.ItemOwnershipPolicy_AssignCreatorPolicy_displayName();
        }       
    }
}
