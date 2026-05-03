/* eslint-disable no-undef */
import {
  AdminLifecycleStages,
  AdminState,
  getAdminStateLabel,
  getAdminWorkViewsByStage,
} from './adminLifecycle'

test('maps legacy and maintenance admin states to display labels', () => {
  expect(getAdminStateLabel('Open')).toEqual(AdminState.Opened)
  expect(getAdminStateLabel(AdminState.MaintenanceLawyerReview)).toEqual(
    AdminState.MaintenanceLawyerReview,
  )
  expect(getAdminStateLabel(AdminState.MaintenanceClientWait)).toEqual(
    AdminState.MaintenanceClientWait,
  )
  expect(getAdminStateLabel(AdminState.Active)).toEqual(AdminState.Active)
  expect(getAdminStateLabel(AdminState.ClosingReview)).toEqual(
    AdminState.ClosingReview,
  )
  expect(getAdminStateLabel(AdminState.Archived)).toEqual(AdminState.Archived)
})

test('keeps admin stages fixed and treats queues as stage-scoped work views', () => {
  expect(AdminLifecycleStages).toEqual([
    'Onboarding',
    'Opening',
    'Maintenance',
    'Closing',
    'Archived',
  ])

  expect(
    getAdminWorkViewsByStage('Opening').map((workView) => workView.title),
  ).toEqual([
    'Ready to Open',
    'Ready for Lawyer',
    'Waiting on Client',
    'Opening Exceptions',
  ])
})
