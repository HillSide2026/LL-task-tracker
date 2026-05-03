export const ADMIN_OPENING_CONTROL_CASE_DEF_ID = 'matter-admin-opening-control'

export const AdminLifecycleStage = {
  Onboarding: 'Onboarding',
  Opening: 'Opening',
  Maintenance: 'Maintenance',
  Closing: 'Closing',
  Archived: 'Archived',
}

export const AdminLifecycleStages = [
  AdminLifecycleStage.Onboarding,
  AdminLifecycleStage.Opening,
  AdminLifecycleStage.Maintenance,
  AdminLifecycleStage.Closing,
  AdminLifecycleStage.Archived,
]

export const AdminState = {
  IntakeReview: 'Intake Review',
  AwaitingEngagement: 'Awaiting Engagement',
  ReadyToOpen: 'Ready to Open',
  ReadyForLawyer: 'Ready for Lawyer',
  WaitingOnClient: 'Waiting on Client',
  Opened: 'Opened',
  Active: 'Active',
  MaintenanceLawyerReview: 'Maintenance Lawyer Review',
  MaintenanceClientWait: 'Maintenance Client Wait',
  WaitingOnExternal: 'Waiting on External',
  ClosingReview: 'Closing Review',
  Closed: 'Closed',
  Archived: 'Archived',
  Open: 'Opened',
}

export const AdminWorkViews = [
  {
    id: 'admin-onboarding-intake',
    title: 'Intake Review',
    stage: AdminLifecycleStage.Onboarding,
    url: '/case-list/admin-opening/intake',
  },
  {
    id: 'admin-onboarding-engagement',
    title: 'Awaiting Engagement',
    stage: AdminLifecycleStage.Onboarding,
    url: '/case-list/admin-opening/engagement-hold',
  },
  {
    id: 'admin-opening-ready',
    title: 'Ready to Open',
    stage: AdminLifecycleStage.Opening,
    url: '/case-list/admin-opening/ready-to-open',
  },
  {
    id: 'admin-opening-lawyer',
    title: 'Ready for Lawyer',
    stage: AdminLifecycleStage.Opening,
    url: '/case-list/admin-opening/lawyer-review',
  },
  {
    id: 'admin-opening-client-waiting',
    title: 'Waiting on Client',
    stage: AdminLifecycleStage.Opening,
    url: '/case-list/admin-opening/client-waiting',
  },
  {
    id: 'admin-opening-exceptions',
    title: 'Opening Exceptions',
    stage: AdminLifecycleStage.Opening,
    url: '/case-list/admin-opening/exceptions',
  },
  {
    id: 'admin-maintenance-active',
    title: 'Active Matters',
    stage: AdminLifecycleStage.Maintenance,
    url: '/case-list/admin-maintenance/active',
  },
  {
    id: 'admin-maintenance-client-waiting',
    title: 'Client Waiting',
    stage: AdminLifecycleStage.Maintenance,
    url: '/case-list/admin-maintenance/client-waiting',
  },
  {
    id: 'admin-maintenance-external-waiting',
    title: 'External Waiting',
    stage: AdminLifecycleStage.Maintenance,
    url: '/case-list/admin-maintenance/external-waiting',
  },
  {
    id: 'admin-maintenance-lawyer-response',
    title: 'Lawyer Response',
    stage: AdminLifecycleStage.Maintenance,
    url: '/case-list/admin-maintenance/lawyer-response',
  },
  {
    id: 'admin-maintenance-exceptions',
    title: 'Maintenance Exceptions',
    stage: AdminLifecycleStage.Maintenance,
    url: '/case-list/admin-maintenance/exceptions',
  },
]

export const AdminTransition = {
  SubmitIntakeReview: 'submitIntakeReview',
  MarkAwaitingEngagement: 'markAwaitingEngagement',
  MarkReadyToOpen: 'markReadyToOpen',
  SendToLawyerReview: 'sendToLawyerReview',
  LawyerApproveOpen: 'lawyerApproveOpen',
  LawyerReturnForFixes: 'lawyerReturnForFixes',
  StartClientWait: 'startClientWait',
  ResumeFromClientWait: 'resumeFromClientWait',
  ActivateMatter: 'activateMatter',
  UpdateMaintenanceControl: 'updateMaintenanceControl',
  SendToMaintenanceLawyerReview: 'sendToMaintenanceLawyerReview',
  LawyerReturnToActive: 'lawyerReturnToActive',
  StartMaintenanceClientWait: 'startMaintenanceClientWait',
  ResumeFromMaintenanceClientWait: 'resumeFromMaintenanceClientWait',
  StartExternalWait: 'startExternalWait',
  ResumeFromExternalWait: 'resumeFromExternalWait',
  LawyerRequestClientFollowup: 'lawyerRequestClientFollowup',
  LawyerRequestExternalFollowup: 'lawyerRequestExternalFollowup',
  StartClosingReview: 'startClosingReview',
  CloseMatter: 'closeMatter',
  ArchiveMatter: 'archiveMatter',
}

export function isAdminLifecycleCase(aCase) {
  return aCase?.caseDefinitionId === ADMIN_OPENING_CONTROL_CASE_DEF_ID
}

export function getAdminStateLabel(adminState) {
  switch (adminState) {
    case 'Open':
      return AdminState.Opened
    default:
      return adminState || '-'
  }
}

export function getAdminWorkViewsByStage(stage) {
  return AdminWorkViews.filter((workView) => workView.stage === stage)
}
