import { Grid, Typography } from '@mui/material'
import { AdminWorkViews } from 'common/adminLifecycle'
import DashboardCard from 'components/cards/DashboardCard'
import {
  IconAlertTriangle,
  IconArchive,
  IconChecklist,
  IconList,
  IconScale,
  IconSquareAsterisk,
} from '@tabler/icons-react'
import { useTranslation } from 'react-i18next'

const workViewIcons = {
  'admin-onboarding-intake': <IconChecklist />,
  'admin-opening-lawyer': <IconScale />,
  'admin-opening-exceptions': <IconAlertTriangle />,
  'admin-maintenance-active': <IconArchive />,
  'admin-maintenance-client-waiting': <IconChecklist />,
  'admin-maintenance-external-waiting': <IconSquareAsterisk />,
  'admin-maintenance-lawyer-response': <IconScale />,
  'admin-maintenance-exceptions': <IconAlertTriangle />,
}

const dashboardWorkViewIds = [
  'admin-onboarding-intake',
  'admin-opening-lawyer',
  'admin-opening-exceptions',
  'admin-maintenance-active',
  'admin-maintenance-client-waiting',
  'admin-maintenance-external-waiting',
  'admin-maintenance-lawyer-response',
  'admin-maintenance-exceptions',
]

const DashboardDefault = () => {
  const { t } = useTranslation()
  const dashboardWorkViews = AdminWorkViews.filter((workView) =>
    dashboardWorkViewIds.includes(workView.id),
  )

  return (
    <Grid container rowSpacing={4.5} columnSpacing={2.75}>
      {/* row 1 */}
      <Grid item xs={12} sx={{ mb: -2.25 }}>
        <Typography variant='h5'>{t('pages.dashboard.title')}</Typography>
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title={t('pages.dashboard.cards.wipcases.label')}
          icon={<IconArchive />}
          to='/case-list/wip-cases'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title={t('pages.dashboard.cards.caselist.label')}
          icon={<IconSquareAsterisk />}
          to='/case-list/cases'
        />
      </Grid>
      <Grid item xs={12} sm={6} md={4} lg={3}>
        <DashboardCard
          title={t('pages.dashboard.cards.tasklist.label')}
          icon={<IconList />}
          to='/task-list'
        />
      </Grid>
      {dashboardWorkViews.map((workView) => (
        <Grid item xs={12} sm={6} md={4} lg={3} key={workView.id}>
          <DashboardCard
            title={workView.title}
            subtitle={`${workView.stage} work view`}
            icon={workViewIcons[workView.id] || <IconList />}
            to={workView.url}
          />
        </Grid>
      ))}

      <Grid
        item
        md={8}
        sx={{ display: { sm: 'none', md: 'block', lg: 'none' } }}
      />
    </Grid>
  )
}

export default DashboardDefault
